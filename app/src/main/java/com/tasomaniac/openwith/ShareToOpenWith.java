package com.tasomaniac.openwith;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.util.Utils;

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

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getApp(this).getAnalytics().sendScreenView("ShareToOpenWith");

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        CharSequence text = reader.getText();
        if (text == null) {
            text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        }
        String foundUrl = findFirstUrl(text);

        if (foundUrl != null) {
            Intent intentToHandle = new Intent(Intent.ACTION_VIEW, Uri.parse(fixUrls(foundUrl)));
            startActivity(intentToHandle
                    .putExtra(ShareCompat.EXTRA_CALLING_PACKAGE, reader.getCallingPackage())
                    .putExtra(ResolverActivity.EXTRA_PRIORITY_PACKAGES, PRIORITY_PACKAGES)
                    .setClass(this, ResolverActivity.class));
        } else {
            Toast.makeText(this, R.string.error_invalid_url, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private String findFirstUrl(@Nullable CharSequence text) {
        if (text == null) {
            return null;
        }
        final Matcher matcher = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String fixUrls(String foundUrl) {
        foundUrl = fixTwitterUrl(foundUrl);
        foundUrl = fixEbayUrl(foundUrl);
        foundUrl = fixDailyMailUrl(foundUrl);
        foundUrl = fixAmazonUrl(foundUrl);
        return foundUrl;
    }

    private static String fixTwitterUrl(String foundUrl) {
        return foundUrl.replace("//mobile.twitter.com", "//twitter.com");
    }

    private static String fixEbayUrl(String foundUrl) {
        final String ebayItemId = Utils.extractEbayItemId(foundUrl);
        if (ebayItemId != null) {
            return "http://pages.ebay.com/link/?nav=item.view&id=" + ebayItemId;
        }
        return foundUrl;
    }

    private static String fixAmazonUrl(String foundUrl) {
        String asin = Utils.extractAmazonASIN(foundUrl);

        //Use fake ASIN to make Amazon App popup for the Intent.
        final Matcher matcher = Pattern.compile("((?:http|https)://)?www\\.amazon\\.(?:com|co\\.uk|co\\.jp|de)/?")
                .matcher(foundUrl);
        if (matcher.matches()) {
            asin = "0000000000";
        }

        if (asin != null) {
            foundUrl = "http://www.amazon.com/gp/aw/d/" + asin + "/aiv/detailpage/";
        }
        return foundUrl;
    }

    private static String fixDailyMailUrl(String foundUrl) {
        String articleId = Utils.extractDailyMailArticleId(foundUrl);
        if (articleId != null) {
            foundUrl = "dailymail://article/" + articleId;
        }
        return foundUrl;
    }
}
