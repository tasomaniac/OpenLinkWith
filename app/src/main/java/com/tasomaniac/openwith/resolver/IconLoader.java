package com.tasomaniac.openwith.resolver;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import timber.log.Timber;

public class IconLoader {

    private final PackageManager packageManager;
    private final int iconDpi;

    public IconLoader(PackageManager packageManager, int iconDpi) {
        this.packageManager = packageManager;
        this.iconDpi = iconDpi;
    }

    Drawable loadFor(ResolveInfo ri) {
        try {
            if (ri.resolvePackageName != null && ri.icon != 0) {
                return getIcon(ri.resolvePackageName, ri.icon);
            }
            final int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                return getIcon(ri.activityInfo.packageName, iconRes);
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Timber.e(e, "Couldn't find resources for package");
        }
        return ri.loadIcon(packageManager);
    }

    @SuppressWarnings("deprecation")
    private Drawable getIcon(String packageName, int resId) throws PackageManager.NameNotFoundException {
        Resources res = packageManager.getResourcesForApplication(packageName);
        return res.getDrawableForDensity(resId, iconDpi);
    }
}
