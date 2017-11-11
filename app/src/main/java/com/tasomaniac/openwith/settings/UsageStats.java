package com.tasomaniac.openwith.settings;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class UsageStats {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isEnabled(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
