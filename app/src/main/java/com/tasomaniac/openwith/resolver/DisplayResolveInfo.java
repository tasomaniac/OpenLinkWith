package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public final class DisplayResolveInfo {
    int id;
    ResolveInfo ri;
    CharSequence displayLabel;
    Drawable displayIcon;
    CharSequence extendedInfo;
    Intent origIntent;

    public DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                              CharSequence pInfo, Intent pOrigIntent) {
        this(0, pri, pLabel, pInfo, pOrigIntent);
    }

    public DisplayResolveInfo(int id, ResolveInfo pri, CharSequence pLabel,
                              CharSequence pInfo, Intent pOrigIntent) {
        this.id = id;
        ri = pri;
        displayLabel = pLabel;
        extendedInfo = pInfo;
        origIntent = pOrigIntent;
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