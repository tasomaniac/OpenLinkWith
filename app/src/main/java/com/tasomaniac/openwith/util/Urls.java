package com.tasomaniac.openwith.util;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

public final class Urls {

    private static final Pattern URL_PATTERN = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE);

    @Nullable
    public static String extractUrlFrom(Intent intent, ShareCompat.IntentReader reader) {
        CharSequence text = intent.getDataString();
        if (text == null) {
            text = reader.getText();
        }
        if (text == null) {
            text = getExtraSelectedText(intent);
        }
        String firstUrl = findFirstUrl(text);
        return fixHttpPrefix(firstUrl);
    }

    @SuppressLint("InlinedApi")
    private static CharSequence getExtraSelectedText(Intent intent) {
        return intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
    }

    @Nullable
    public static String findFirstUrl(@Nullable CharSequence text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = URL_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String url = matcher.group();
        if (url.startsWith("content://") || url.startsWith("file://")) {
            return null;
        }
        return url;
    }

    @Nullable
    private static String fixHttpPrefix(@Nullable String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("http")) {
            return url;
        }
        return "https://" + url;
    }

    private Urls() {
    }
}
