package com.tasomaniac.openwith;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ShareCompat;

import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.util.CallerPackageExtractor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import dagger.android.DaggerActivity;

import static com.tasomaniac.openwith.extensions.UrlsKt.extractUrlFrom;

public class ShareToOpenWith extends DaggerActivity {

    @Inject Analytics analytics;
    @Inject Provider<CallerPackageExtractor> callerPackageExtractor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ignore special case for setting OLW as default browser.
        if ("https://".equals(getIntent().getDataString())) {
            finish();
            return;
        }

        analytics.sendScreenView("ShareToOpenWith");

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = extractUrlFrom(getIntent(), reader);

        if (foundUrl != null) {
            trackLinkOpen();

            String callerPackage = callerPackageExtractor.get().extract();
            Intent intent = RedirectFixActivity.createIntent(this, foundUrl)
                    .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, callerPackage);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void trackLinkOpen() {
        analytics.sendEvent(
                "Open Link",
                "Set as browser",
                Boolean.toString(isSetAsBrowser(getIntent()))
        );
    }

    private static boolean isSetAsBrowser(Intent intent) {
        return intent.getData() != null;
    }

}
