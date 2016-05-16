package com.tasomaniac.openwith;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;

import com.tasomaniac.openwith.resolver.ResolverActivity;

import static com.tasomaniac.openwith.util.Urls.fixUrls;

public class ShareToOpenWith extends BaseShareActivity {

    private static final String[] PRIORITY_PACKAGES = new String[] {
            "com.whatsapp",
            "com.twitter.android",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.google.android.talk",
            "com.google.android.apps.plus",
            "com.google.android.apps.photos",
            "com.pandora.android",
            "com.instagram.android",
            "com.linkedin.android",
            "com.spotify.music",
            "com.pinterest",
            "com.medium.reader",
            "com.ubercab",
            "com.meetup",
            "com.tumblr",
            "com.badoo.mobile",
            "tv.periscope.android",
            "com.skype.raider"
    };

    public static final String EXTRA_FROM_DIRECT_SHARE = "EXTRA_FROM_DIRECT_SHARE";

    private static boolean isFromDirectShare(Intent intent) {
        return intent.getBooleanExtra(EXTRA_FROM_DIRECT_SHARE, false);
    }

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getApp(this).getAnalytics().sendScreenView("ShareToOpenWith");
        if (isFromDirectShare(getIntent())) {
            App.getApp(this).getAnalytics().sendEvent(
                    "Direct Share",
                    "Clicked",
                    "true"
            );
        }

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        String foundUrl = extractUrlFrom(reader);

        if (foundUrl != null) {
            Intent intentToHandle = new Intent(Intent.ACTION_VIEW, Uri.parse(fixUrls(foundUrl)));
            startActivity(intentToHandle
                    .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, reader.getCallingPackage())
                    .putExtra(ResolverActivity.EXTRA_PRIORITY_PACKAGES, PRIORITY_PACKAGES)
                    .setClass(this, ResolverActivity.class));
        } else {
            showErrorInvalidUrl();
        }

        finish();
    }
}
