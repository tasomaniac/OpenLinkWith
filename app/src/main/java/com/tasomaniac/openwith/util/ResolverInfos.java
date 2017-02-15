package com.tasomaniac.openwith.util;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

public class ResolverInfos {
    public static ComponentName componentName(ResolveInfo ri) {
        ActivityInfo ai = ri.activityInfo;
        return new ComponentName(ai.applicationInfo.packageName, ai.name);
    }

    public static boolean equals(ResolveInfo left, ResolveInfo right) {
        return componentName(left).equals(componentName(right));
    }
}
