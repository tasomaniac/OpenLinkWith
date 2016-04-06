package com.tasomaniac.openwith.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

import com.tasomaniac.openwith.App;

import java.util.List;

import timber.log.Timber;

public class Intents {

    /**
     * Queries on-device packages for a handler for the supplied {@link Intent}.
     */
    public static boolean hasHandler(Context context, Intent intent) {
        List<ResolveInfo> handlers = context.getPackageManager().queryIntentActivities(intent, 0);
        return !handlers.isEmpty();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean maybeStartUsageAccessSettings(final Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return true;
        } catch (Exception e) {
            Timber.e(e, "Usage Access Open");

            App.getApp(activity).getAnalytics().sendEvent("Usage Access", "Not Found", null);
        }

        return false;
    }

    private Intents() {
    }
}
