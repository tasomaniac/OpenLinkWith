package com.tasomaniac.openwith.resolver;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public final class DisplayResolveInfo {
    int id;
    ResolveInfo ri;
    CharSequence displayLabel;
    Drawable displayIcon;
    CharSequence extendedInfo;

    public DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                              CharSequence pInfo) {
        this(0, pri, pLabel, pInfo);
    }

    public DisplayResolveInfo(int id, ResolveInfo pri, CharSequence pLabel,
                              CharSequence pInfo) {
        this.id = id;
        ri = pri;
        displayLabel = pLabel;
        extendedInfo = pInfo;
    }

    public int getId() {
        return id;
    }

    public CharSequence getDisplayLabel() {
        return displayLabel;
    }

    public CharSequence getExtendedInfo() {
        return extendedInfo;
    }
}
