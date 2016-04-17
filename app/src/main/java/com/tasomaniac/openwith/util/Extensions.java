package com.tasomaniac.openwith.util;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extensions {

    @Nullable
    public static String extractAmazonASIN(String foundUrl) {
        try {
            final Matcher matcher = Pattern.compile(".*//www.amazon.(?:com|co\\.uk|co.jp|de)/gp/aw/d/(\\w{10})/.*", Pattern.CASE_INSENSITIVE)
                    .matcher(foundUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception ignored) {
        }
        try {
            //http://www.amazon.com/Garmin-Speed-Cadence-Bike-Sensor/dp/B000BFNOT8
            final Matcher matcher = Pattern.compile(".*//www.amazon.(?:com|co\\.uk|co.jp|de)/(?:.+/)?dp/(\\w{10}).*", Pattern.CASE_INSENSITIVE)
                    .matcher(foundUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public static String extractEbayItemId(String foundUrl) {
        try {
            final Matcher matcher = Pattern.compile("(?:(?:http|https)://)?(?:www|m).ebay.(?:com|co\\.uk|com.hk|com.au|at|ca|fr|de|ie|it|com\\.my|nl|ph|pl|com\\.sg|es|ch)/itm/(?:.*/)?(\\d+)(?:\\?.*)?")
                    .matcher(foundUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public static String extractDailyMailArticleId(String foundUrl) {
        try {
            final Matcher matcher = Pattern.compile("(?:(?:http|https)://)?(?:www|m).dailymail.co.uk/.*/article-(\\d*)?/.*")
                    .matcher(foundUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Extensions() {
    }
}
