package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import dagger.Lazy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class ResolveListGrouper {

    private final PackageManager packageManager;
    private final IconLoader iconLoader;
    private final Lazy<ResolverComparator> resolverComparator;

    @Nullable private ComponentName lastChosenComponent;

    boolean showExtended;
    @Nullable DisplayActivityInfo filteredItem;

    @Inject ResolveListGrouper(
            PackageManager packageManager,
            IconLoader iconLoader,
            Lazy<ResolverComparator> resolverComparator) {
        this.packageManager = packageManager;
        this.iconLoader = iconLoader;
        this.resolverComparator = resolverComparator;
    }

    /**
     * Taken from AOSP, don't try to understand what's going on.
     */
    List<DisplayActivityInfo> groupResolveList(
            List<ResolveInfo> current,
            @Nullable ComponentName lastChosenComponent
    ) {
        this.lastChosenComponent = lastChosenComponent;
        Collections.sort(current, resolverComparator.get());
        filteredItem = null;
        showExtended = false;
        List<DisplayActivityInfo> grouped = new ArrayList<>();

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
    private void processGroup(List<DisplayActivityInfo> grouped,
                              List<ResolveInfo> current,
                              int start,
                              int end,
                              ResolveInfo ro,
                              CharSequence displayLabel) {
        // Process labels from start to i
        int num = end - start + 1;
        if (num == 1) {
            // No duplicate labels. Use label for entry at start
            Drawable icon = iconLoader.loadFor(ro.activityInfo);
            DisplayActivityInfo activityInfo = new DisplayActivityInfo(ro.activityInfo, displayLabel, null, icon);
            if (isLastChosenPosition(ro.activityInfo)) {
                filteredItem = activityInfo;
            } else {
                grouped.add(activityInfo);
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
                ActivityInfo add = current.get(k).activityInfo;
                DisplayActivityInfo activityInfo = displayResolveInfoToAdd(usePkg, add, displayLabel);
                if (isLastChosenPosition(add)) {
                    filteredItem = activityInfo;
                } else {
                    grouped.add(activityInfo);
                }
            }
        }
    }

    private DisplayActivityInfo displayResolveInfoToAdd(boolean usePackageName, ActivityInfo activityInfo, CharSequence displayLabel) {
        Drawable icon = iconLoader.loadFor(activityInfo);
        if (usePackageName) {
            // Use package name for all entries from start to end-1
            return new DisplayActivityInfo(activityInfo, displayLabel, activityInfo.packageName, icon);
        } else {
            // Use application name for all entries from start to end-1
            CharSequence extendedLabel = activityInfo.applicationInfo.loadLabel(packageManager);
            return new DisplayActivityInfo(activityInfo, displayLabel, extendedLabel, icon);
        }
    }

    private boolean isLastChosenPosition(ActivityInfo activityInfo) {
        return lastChosenComponent != null
                && lastChosenComponent.getPackageName().equals(activityInfo.packageName)
                && lastChosenComponent.getClassName().equals(activityInfo.name);
    }

}
