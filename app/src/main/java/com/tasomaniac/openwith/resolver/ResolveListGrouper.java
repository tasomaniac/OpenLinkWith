package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class ResolveListGrouper {

    private final PackageManager packageManager;
    @Nullable private final ComponentName lastChosenComponent;

    boolean showExtended;
    @Nullable DisplayResolveInfo filteredItem;

    ResolveListGrouper(PackageManager packageManager, @Nullable ComponentName lastChosenComponent) {
        this.packageManager = packageManager;
        this.lastChosenComponent = lastChosenComponent;
    }

    /**
     * Taken from AOSP, don't try to understand what's going on.
     */
    List<DisplayResolveInfo> groupResolveList(List<ResolveInfo> current) {
        filteredItem = null;
        showExtended = false;
        List<DisplayResolveInfo> grouped = new ArrayList<>();

        // Check for applications with same name and use application name or
        // package name if necessary
        ResolveInfo r0 = current.get(0);
        int start = 0;
        CharSequence r0Label = r0.loadLabel(packageManager);
        int size = current.size();
        for (int i = 1; i < size; i++) {
            if (r0Label == null) {
                r0Label = r0.activityInfo.packageName;
            }
            ResolveInfo ri = current.get(i);
            CharSequence riLabel = ri.loadLabel(packageManager);
            if (riLabel == null) {
                riLabel = ri.activityInfo.packageName;
            }
            if (riLabel.equals(r0Label)) {
                continue;
            }
            processGroup(grouped, current, start, (i - 1), r0, r0Label);
            r0 = ri;
            r0Label = riLabel;
            start = i;
        }
        // Process last group
        processGroup(grouped, current, start, (size - 1), r0, r0Label);
        return grouped;
    }

    /**
     * Taken from AOSP, don't try to understand what's going on.
     */
    private void processGroup(List<DisplayResolveInfo> grouped,
                              List<ResolveInfo> current,
                              int start,
                              int end,
                              ResolveInfo ro,
                              CharSequence displayLabel) {
        // Process labels from start to i
        int num = end - start + 1;
        if (num == 1) {
            // No duplicate labels. Use label for entry at start
            DisplayResolveInfo dri = new DisplayResolveInfo(ro, displayLabel, null);
            if (isLastChosenPosition(ro)) {
                filteredItem = dri;
            } else {
                grouped.add(dri);
            }
        } else {
            showExtended = true;
            boolean usePkg = false;
            CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(packageManager);
            if (startApp == null) {
                usePkg = true;
            }
            if (!usePkg) {
                // Use HashSet to track duplicates
                HashSet<CharSequence> duplicates = new HashSet<>();
                duplicates.add(startApp);
                for (int j = start + 1; j <= end; j++) {
                    ResolveInfo jRi = current.get(j);
                    CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(packageManager);
                    if ((jApp == null) || (duplicates.contains(jApp))) {
                        usePkg = true;
                        break;
                    } else {
                        duplicates.add(jApp);
                    }
                }
                // Clear HashSet for later use
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                ResolveInfo add = current.get(k);
                DisplayResolveInfo dri = displayResolveInfoToAdd(usePkg, add, displayLabel);
                if (isLastChosenPosition(add)) {
                    filteredItem = dri;
                } else {
                    grouped.add(dri);
                }
            }
        }
    }

    private DisplayResolveInfo displayResolveInfoToAdd(boolean usePackageName, ResolveInfo add, CharSequence displayLabel) {
        if (usePackageName) {
            // Use package name for all entries from start to end-1
            return new DisplayResolveInfo(add, displayLabel, add.activityInfo.packageName);
        } else {
            // Use application name for all entries from start to end-1
            CharSequence extendedLabel = add.activityInfo.applicationInfo.loadLabel(packageManager);
            return new DisplayResolveInfo(add, displayLabel, extendedLabel);
        }
    }

    private boolean isLastChosenPosition(ResolveInfo info) {
        return lastChosenComponent != null
                && lastChosenComponent.getPackageName().equals(info.activityInfo.packageName)
                && lastChosenComponent.getClassName().equals(info.activityInfo.name);
    }

}
