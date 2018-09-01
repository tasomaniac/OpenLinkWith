package com.tasomaniac.openwith;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.app.ShareCompat;
import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.util.CallerPackageExtractor;
import com.tasomaniac.openwith.util.Urls;
import dagger.android.DaggerActivity;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class ShareToOpenWith extends DaggerActivity {

    public static final String EXTRA_FROM_DIRECT_SHARE = "EXTRA_FROM_DIRECT_SHARE";

    private static boolean isFromDirectShare(Intent intent) {
        return intent.getBooleanExtra(EXTRA_FROM_DIRECT_SHARE, false);
    }

    @Inject Analytics analytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            Intent intent = RedirectFixActivity.createIntent(this, foundUrl)
                    .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, callerPackage);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

}
