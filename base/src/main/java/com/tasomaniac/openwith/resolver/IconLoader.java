package com.tasomaniac.openwith.resolver;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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

    public Drawable loadFor(ActivityInfo activity) {
        try {
            String packageName = activity.packageName;
            if (activity.icon != 0) {
                return getIcon(packageName, activity.icon);
            }
            final int iconRes = activity.getIconResource();
            if (iconRes != 0) {
                return getIcon(packageName, iconRes);
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Timber.e(e, "Couldn't find resources for package");
        }
        return activity.loadIcon(packageManager);
    }

    private Drawable getIcon(String packageName, int resId) throws PackageManager.NameNotFoundException {
        Resources res = packageManager.getResourcesForApplication(packageName);
        return res.getDrawableForDensity(resId, iconDpi);
    }
}
