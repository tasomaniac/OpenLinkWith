package com.tasomaniac.openwith;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareToOpenWith extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);

        boolean urlHandled = false;
        String foundUrl = findFirstUrl(reader.getText());
        if (foundUrl == null) {
            foundUrl = findFirstUrl(reader.getHtmlText());
        }

        if (foundUrl != null) {
            Intent intentToHandle = new Intent(Intent.ACTION_VIEW, Uri.parse(foundUrl));

            final ResolveInfo resolveInfo = getPackageManager()
                    .resolveActivity(intentToHandle, PackageManager.MATCH_DEFAULT_ONLY);
        }

        if (!urlHandled) {
            Toast.makeText(this, "The shared content did not have any valid URLs.", Toast.LENGTH_SHORT).show();
        }
    }

    private String findFirstUrl(CharSequence text) {

        final Matcher matcher = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))\n", Pattern.CASE_INSENSITIVE)
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
