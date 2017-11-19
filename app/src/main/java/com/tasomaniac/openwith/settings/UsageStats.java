package com.tasomaniac.openwith.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class UsageStats {

    public static Completable observeAccessGiven(Context context) {
        return observe(context)
                .filter(accessGiven -> accessGiven)
                .firstElement()
                .timeout(1, TimeUnit.MINUTES)
                .onErrorReturnItem(false)
                .ignoreElement();
    }

    private static Observable<Boolean> observe(Context context) {
        return Observable.interval(1, TimeUnit.SECONDS)
                .map(ignored -> isEnabled(context));
    }

    public static boolean isEnabled(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @SuppressLint("InlinedApi")
    public static boolean maybeStartUsageAccessSettings(Activity activity) {
        try {
            activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return true;
        } catch (Exception e) {
            Timber.e(e, "Usage Access Open");
            return false;
        }
    }
}
