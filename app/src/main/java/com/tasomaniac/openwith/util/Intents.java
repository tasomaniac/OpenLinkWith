package com.tasomaniac.openwith.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;

import java.util.List;

import timber.log.Timber;

/**
 * Created by tasomaniac on 6/1/16.
 */
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
        } catch (ActivityNotFoundException e) {
            String error = activity.getString(R.string.error_usage_access_not_found);

            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
            Timber.e(e, error);

            App.getApp(activity).getAnalytics().sendEvent("Usage Access", "Not Found", null);
        }

        return false;
    }

    private Intents() {
    }
}
