package com.tasomaniac.openwith.resolver;

import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;

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
