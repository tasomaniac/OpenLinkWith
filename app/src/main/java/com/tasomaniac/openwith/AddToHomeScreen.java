package com.tasomaniac.openwith;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;

import com.tasomaniac.openwith.resolver.ResolverActivity;

import static com.tasomaniac.openwith.util.Urls.fixUrls;

public class AddToHomeScreen extends BaseShareActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getApp(this).getAnalytics().sendScreenView("AddToHomeScreen");

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = extractUrlFrom(reader);

        if (foundUrl != null) {
            Intent intentToHandle = new Intent(Intent.ACTION_VIEW, Uri.parse(fixUrls(foundUrl)));
            startActivity(intentToHandle.setClass(this, ResolverActivity.class));
        } else {
            showErrorInvalidUrl();
        }

        finish();
    }
}
