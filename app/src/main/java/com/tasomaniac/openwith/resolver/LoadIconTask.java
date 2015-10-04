package com.tasomaniac.openwith.resolver;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import timber.log.Timber;

public abstract class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {

    PackageManager mPm;
    int mIconDpi;

    public LoadIconTask(PackageManager mPm, int mIconDpi) {
        this.mPm = mPm;
        this.mIconDpi = mIconDpi;
    }

    @Override
    protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
        final DisplayResolveInfo info = params[0];
        if (info.displayIcon == null) {
            info.displayIcon = loadIconForResolveInfo(mPm, info.ri, mIconDpi);
        }
        return info;
    }

    public static Drawable loadIconForResolveInfo(PackageManager mPm, ResolveInfo ri, int mIconDpi) {
        Drawable dr;
        try {
            if (ri.resolvePackageName != null && ri.icon != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon, mIconDpi);
                if (dr != null) {
                    return dr;
                }
            }
            final int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes, mIconDpi);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Couldn't find resources for package");
        }
        return ri.loadIcon(mPm);
    }

    public static Drawable getIcon(Resources res, int resId, int mIconDpi) {
        Drawable result;
        try {
            //noinspection deprecation
            result = res.getDrawableForDensity(resId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            result = null;
        }

        return result;
    }
}
