package com.tasomaniac.openwith;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.CallerPackageExtractor;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Urls;

import timber.log.Timber;

import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.withHost;
import static com.tasomaniac.openwith.util.Urls.fixUrls;

public class ShareToOpenWith extends Activity {

    public static final String EXTRA_FROM_DIRECT_SHARE = "EXTRA_FROM_DIRECT_SHARE";

    private static boolean isFromDirectShare(Intent intent) {
        return intent.getBooleanExtra(EXTRA_FROM_DIRECT_SHARE, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Analytics analytics = Injector.obtain(this).analytics();
        analytics.sendScreenView("ShareToOpenWith");
        if (isFromDirectShare(getIntent())) {
            analytics.sendEvent(
                    "Direct Share",
                    "Clicked",
                    "true"
            );
        }

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = Urls.extractUrlFrom(getIntent(), reader);

        if (foundUrl == null) {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String callerPackage = CallerPackageExtractor.from(this).extract();
        Uri uri = Uri.parse(fixUrls(foundUrl));
        PreferredResolver preferredResolver = new PreferredResolver(getPackageManager(), getContentResolver(), callerPackage);
        preferredResolver.resolve(uri);

        if (preferredResolver.shouldStartPreferred()) {
            String warning = getString(R.string.warning_open_link_with_name, preferredResolver.loadLabel());
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();

            try {
                Intents.startActivityFixingIntent(this, preferredResolver.preferredIntent()
                        .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                          | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
                finish();
                return;
            } catch (SecurityException e) {
                Timber.e(e, "Security Exception for the url %s", uri);
                getContentResolver().delete(withHost(uri.getHost()), null, null);
            }
        }

        Intent resolverIntent = new Intent(Intent.ACTION_VIEW, uri)
                .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, callerPackage)
                .putExtra(ResolverActivity.EXTRA_LAST_CHOSEN_COMPONENT, preferredResolver.lastChosenComponent())
                .setClass(this, ResolverActivity.class);
        startActivity(resolverIntent);
        finish();
    }

}
