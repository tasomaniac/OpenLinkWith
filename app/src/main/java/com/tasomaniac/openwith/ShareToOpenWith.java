package com.tasomaniac.openwith;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.CallerPackageExtractor;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.Urls;

import net.simonvt.schematic.Cursors;

import timber.log.Timber;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.*;
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
        Intent intentToHandle = new Intent(Intent.ACTION_VIEW, uri);

        ComponentName lastChosenComponent = null;
        final Cursor query = queryIntentWith(uri.getHost());
        if (query != null && query.moveToFirst()) {
            try {
                final boolean isPreferred = Cursors.getInt(query, PREFERRED) == 1;
                final boolean isLastChosen = Cursors.getInt(query, LAST_CHOSEN) == 1;

                if (isPreferred || isLastChosen) {
                    final String componentString = Cursors.getString(query, COMPONENT);

                    lastChosenComponent = ComponentName.unflattenFromString(componentString);
                    ResolveInfo ri = getPackageManager().resolveActivity(
                            new Intent().setComponent(lastChosenComponent),
                            PackageManager.MATCH_DEFAULT_ONLY
                    );

                    if (isPreferred && ri != null) {
                        boolean isCallerPackagePreferred = ri.activityInfo.packageName.equals(callerPackage);
                        if (!isCallerPackagePreferred) {
                            String warning = getString(R.string.warning_open_link_with_name, ri.loadLabel(getPackageManager()));
                            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();

                            intentToHandle.setComponent(lastChosenComponent);
                            intentToHandle.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                                            | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                            try {
                                Intents.startActivityFixingIntent(this, intentToHandle);
                                finish();
                                return;
                            } catch (SecurityException e) {
                                Timber.e(e, "Security Exception for %s", lastChosenComponent.flattenToString());
                                getContentResolver().delete(withHost(uri.getHost()), null, null);
                            }
                        }
                    }
                }
            } finally {
                query.close();
            }
        }

        startActivity(intentToHandle
                              .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, callerPackage)
                              .putExtra(ResolverActivity.EXTRA_LAST_CHOSEN_COMPONENT, lastChosenComponent)
                              .setClass(this, ResolverActivity.class));

        finish();
    }

    @Nullable
    private Cursor queryIntentWith(String host) {
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        return getContentResolver().query(withHost(host), null, null, null, null);
    }
}
