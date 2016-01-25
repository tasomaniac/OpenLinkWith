package com.tasomaniac.openwith.util;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {

    }

    /**
     * Converts dp value to px value.
     *
     * @param res Resources objects to get displayMetrics.
     * @param dp  original dp value.
     * @return px value.
     */
    public static int dpToPx(@NonNull Resources res, int dp) {
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        return px / (metrics.densityDpi / 160f);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isUsageStatsEnabled(final Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

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
}
