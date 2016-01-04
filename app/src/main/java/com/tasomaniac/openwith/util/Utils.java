package com.tasomaniac.openwith.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;

import timber.log.Timber;

public class Utils {
    
    private Utils() {

    }

    /**
     * Converts dp value to px value.
     *
     * @param res Resources objects to get displayMetrics.
     * @param dp original dp value.
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean maybeStartUsageAccessSettings(final Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return true;
        } catch (ActivityNotFoundException e) {
            String error = activity.getString(R.string.error_usage_access_not_found);

            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            Timber.e(e, error);

            App.getApp(activity).getAnalytics().sendEvent("Usage Access", "Not Found", null);
        }

        return false;
    }
}
