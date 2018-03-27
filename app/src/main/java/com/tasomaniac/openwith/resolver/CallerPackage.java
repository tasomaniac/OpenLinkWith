package com.tasomaniac.openwith.resolver;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class CallerPackage {

    @Nullable private final String callerPackage;

    public static CallerPackage from(Activity activity) {
        return new CallerPackage(ShareCompat.getCallingPackage(activity));
    }

    private CallerPackage(@Nullable String callerPackage) {
        this.callerPackage = callerPackage;
    }

    @Nullable
    public String getCallerPackage() {
        return callerPackage;
    }

    void removeFrom(List<ResolveInfo> currentResolveList) {
        if (!TextUtils.isEmpty(callerPackage)) {
            removePackageFromList(callerPackage, currentResolveList);
        }
    }

    private static void removePackageFromList(String packageName, List<ResolveInfo> list) {
        List<ResolveInfo> remove = new ArrayList<>();
        for (ResolveInfo info : list) {
            if (info.activityInfo.packageName.equals(packageName)) {
                remove.add(info);
            }
        }
        list.removeAll(remove);
    }
}
