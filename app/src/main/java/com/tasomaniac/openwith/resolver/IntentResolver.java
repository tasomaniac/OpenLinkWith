package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import dagger.Lazy;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

public class IntentResolver {

    private static final Intent BROWSER_INTENT;

    static {
        BROWSER_INTENT = new Intent();
        BROWSER_INTENT.setAction(Intent.ACTION_VIEW);
        BROWSER_INTENT.addCategory(Intent.CATEGORY_BROWSABLE);
        BROWSER_INTENT.setData(Uri.parse("http:"));
    }

    private final PackageManager packageManager;
    private final Lazy<ResolverComparator> resolverComparator;
    private final Intent sourceIntent;
    private final String callerPackage;
    @Nullable private final ComponentName lastChosenComponent;

    private boolean mShowExtended;
    @Nullable private DisplayResolveInfo filteredItem;
    private Listener listener = Listener.NO_OP;

    public IntentResolver(PackageManager packageManager,
                          Lazy<ResolverComparator> resolverComparator,
                          Intent sourceIntent,
                          String callerPackage,
                          @Nullable ComponentName lastChosenComponent) {
        this.packageManager = packageManager;
        this.resolverComparator = resolverComparator;
        this.sourceIntent = sourceIntent;
        this.callerPackage = callerPackage;
        this.lastChosenComponent = lastChosenComponent;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener == null ? Listener.NO_OP : listener;
    }

    @Nullable
    public DisplayResolveInfo getFilteredItem() {
        return filteredItem;
    }

    /**
     * true if one of the items is filtered and stays at the top header
     */
    boolean hasFilteredItem() {
        return filteredItem != null;
    }

    public boolean shouldShowExtended() {
        return mShowExtended;
    }

    Intent intentForDisplayResolveInfo(DisplayResolveInfo dri) {
        Intent intent = new Intent(sourceIntent);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        if (dri != null && dri.ri != null) {
            ActivityInfo ai = dri.ri.activityInfo;
            if (ai != null) {
                intent.setComponent(new ComponentName(
                        ai.applicationInfo.packageName, ai.name));
            }
        }
        return intent;
    }

    public void rebuildList() {
        List<DisplayResolveInfo> resolved = new ArrayList<>();
        int flag;
        if (SDK_INT >= M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }
        flag = flag | PackageManager.GET_RESOLVED_FILTER;

        List<ResolveInfo> currentResolveList = new ArrayList<>();
        currentResolveList.addAll(packageManager.queryIntentActivities(sourceIntent, flag));

        if (SDK_INT >= M) {
            addBrowsersToList(currentResolveList, flag);
        }

        //Remove the components from the caller
        if (!TextUtils.isEmpty(callerPackage)) {
            removePackageFromList(callerPackage, currentResolveList);
        }

        int N;
        if ((N = currentResolveList.size()) > 0) {
            // Only display the first matches that are either of equal
            // priority or have asked to be default options.
            ResolveInfo r0 = currentResolveList.get(0);
            for (int i = 1; i < N; i++) {
                ResolveInfo ri = currentResolveList.get(i);

                if (r0.priority != ri.priority ||
                        r0.isDefault != ri.isDefault) {
                    while (i < N) {
                        currentResolveList.remove(i);
                        N--;
                    }
                }
            }

            //If there is no left, return
            if (N <= 0) {
                listener.onIntentResolved(resolved, filteredItem);
                return;
            }

            if (N > 1) {
                Collections.sort(currentResolveList, resolverComparator.get());
            }

            // Check for applications with same name and use application name or
            // package name if necessary
            r0 = currentResolveList.get(0);
            int start = 0;
            CharSequence r0Label = r0.loadLabel(packageManager);
            mShowExtended = false;
            for (int i = 1; i < N; i++) {
                if (r0Label == null) {
                    r0Label = r0.activityInfo.packageName;
                }
                ResolveInfo ri = currentResolveList.get(i);
                CharSequence riLabel = ri.loadLabel(packageManager);
                if (riLabel == null) {
                    riLabel = ri.activityInfo.packageName;
                }
                if (riLabel.equals(r0Label)) {
                    continue;
                }
                processGroup(resolved, currentResolveList, start, (i - 1), r0, r0Label);
                r0 = ri;
                r0Label = riLabel;
                start = i;
            }
            // Process last group
            processGroup(resolved, currentResolveList, start, (N - 1), r0, r0Label);
        }
        listener.onIntentResolved(resolved, filteredItem);
    }

    private static void removePackageFromList(final String packageName, List<ResolveInfo> currentResolveList) {
        List<ResolveInfo> infosToRemoved = new ArrayList<>();
        for (ResolveInfo info : currentResolveList) {
            if (info.activityInfo.packageName.equals(packageName)) {
                infosToRemoved.add(info);
            }
        }
        currentResolveList.removeAll(infosToRemoved);
    }

    private void addBrowsersToList(List<ResolveInfo> currentResolveList, int flag) {
        final int initialSize = currentResolveList.size();

        List<ResolveInfo> browsers = queryBrowserIntentActivities(flag);
        for (ResolveInfo browser : browsers) {
            boolean browserFound = false;

            for (int i = 0; i < initialSize; i++) {
                ResolveInfo info = currentResolveList.get(i);

                if (info.activityInfo.packageName.equals(browser.activityInfo.packageName)) {
                    browserFound = true;
                    break;
                }
            }

            if (!browserFound) {
                currentResolveList.add(browser);
            }
        }
    }

    private List<ResolveInfo> queryBrowserIntentActivities(int flags) {
        return packageManager.queryIntentActivities(BROWSER_INTENT, flags);
    }

    private void processGroup(List<DisplayResolveInfo> resolved,
                              List<ResolveInfo> rList,
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
                resolved.add(dri);
            }
        } else {
            mShowExtended = true;
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
                    ResolveInfo jRi = rList.get(j);
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
                ResolveInfo add = rList.get(k);
                DisplayResolveInfo dri = displayResolveInfoToAdd(usePkg, add, displayLabel);
                if (isLastChosenPosition(add)) {
                    filteredItem = dri;
                } else {
                    resolved.add(dri);
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

    interface Listener {
        void onIntentResolved(List<DisplayResolveInfo> list, @Nullable DisplayResolveInfo filteredItem);

        Listener NO_OP = new Listener() {
            @Override
            public void onIntentResolved(List<DisplayResolveInfo> list, @Nullable DisplayResolveInfo filteredItem) {
                // no-op
            }
        };
    }
}
