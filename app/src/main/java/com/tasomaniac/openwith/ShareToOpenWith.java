package com.tasomaniac.openwith;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.tasomaniac.openwith.resolver.ResolverActivity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareToOpenWith extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean urlHandled = false;
        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);

        String foundUrl = findFirstUrl(reader.getText());

        if (foundUrl != null) {
            Intent intentToHandle = new Intent(Intent.ACTION_VIEW, Uri.parse(foundUrl));

            final List<ResolveInfo> resolveInfos = getPackageManager()
                    .queryIntentActivities(intentToHandle, PackageManager.MATCH_DEFAULT_ONLY);

            if (resolveInfos != null && resolveInfos.size() > 0) {
                urlHandled = true;

                startActivity(intentToHandle
                        .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, reader.getCallingPackage())
                        .putExtra(ResolverActivity.EXTRA_PRIORITY_PACKAGES, PRIORITY_PACKAGES)
                        .setClass(this, ResolverActivity.class));
            }
        }

        if (!urlHandled) {
            Toast.makeText(this, "The shared content did not have any valid URLs.", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String findFirstUrl(@Nullable CharSequence text) {
        if (text == null) {
            return null;
        }

        final Matcher matcher = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        while (matcher.find()) {
            final String url = matcher.group();
            if (URLUtil.isValidUrl(url)) {
                return url;
            }
        }
        return null;
    }

}
