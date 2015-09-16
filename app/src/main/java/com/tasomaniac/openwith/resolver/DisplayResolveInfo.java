package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public final class DisplayResolveInfo {
    ResolveInfo ri;
    CharSequence displayLabel;
    Drawable displayIcon;
    CharSequence extendedInfo;
    Intent origIntent;

    DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                       CharSequence pInfo, Intent pOrigIntent) {
        ri = pri;
        displayLabel = pLabel;
        extendedInfo = pInfo;
        origIntent = pOrigIntent;
    }
}