package com.tasomaniac.openwith;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.CallerPackageExtractor;
import com.tasomaniac.openwith.util.Urls;

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

        if (foundUrl != null) {
            String callerPackage = CallerPackageExtractor.from(this).extract();
            Intent intent = ResolverActivity.createIntent(this, foundUrl)
                    .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, callerPackage);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

}
