package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tasomaniac.openwith.rx.SchedulingStrategy;
import com.tasomaniac.openwith.util.Intents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import dagger.Lazy;
import io.reactivex.Observable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

class IntentResolver {

    private static final Intent BROWSER_INTENT;

    static {
        BROWSER_INTENT = new Intent();
        BROWSER_INTENT.setAction(Intent.ACTION_VIEW);
        BROWSER_INTENT.addCategory(Intent.CATEGORY_BROWSABLE);
        BROWSER_INTENT.setData(Uri.parse("http:"));
    }

    private final RedirectFixer redirectFixer;
    private final PackageManager packageManager;
    private final Lazy<ResolverComparator> resolverComparator;
    private final SchedulingStrategy schedulingStrategy;
    private final Intent sourceIntent;
    private final String callerPackage;
    @Nullable private final ComponentName lastChosenComponent;

    private boolean showExtended = false;
    @Nullable private DisplayResolveInfo filteredItem;
    private State state = State.IDLE;
    private Listener listener = Listener.NO_OP;

    IntentResolver(RedirectFixer redirectFixer,
                   PackageManager packageManager,
                   Lazy<ResolverComparator> resolverComparator,
                   SchedulingStrategy schedulingStrategy,
                   Intent sourceIntent,
                   String callerPackage,
                   @Nullable ComponentName lastChosenComponent) {
        this.redirectFixer = redirectFixer;
        this.packageManager = packageManager;
        this.resolverComparator = resolverComparator;
        this.schedulingStrategy = schedulingStrategy;
        this.sourceIntent = sourceIntent;
        this.callerPackage = callerPackage;
        this.lastChosenComponent = lastChosenComponent;
    }

    void setListener(@Nullable Listener listener) {
        this.listener = listener == null ? Listener.NO_OP : listener;
    }

    Intent getSourceIntent() {
        return sourceIntent;
    }

    State getState() {
        return state;
    }

    @Nullable
    DisplayResolveInfo getFilteredItem() {
        return state instanceof Success ? ((Success) state).filteredItem : null;
    }

    /**
     * true if one of the items is filtered and stays at the top header
     */
    boolean hasFilteredItem() {
        return state instanceof Success && ((Success) state).hasFilteredItem();
    }

    void resolve() {
        Observable.just(sourceIntent)
                .map(this::doResolve)
                .flatMap(state -> state.onlyBrowsers
                        ? redirectFixer.followRedirects(sourceIntent).map(this::doResolve).toObservable()
                        : Observable.just(state))
                .cast(State.class)
                .startWith(State.LOADING)
                .compose(schedulingStrategy.apply())
                .subscribe(state -> {
                    this.state = state;
                    state.notify(listener);
                });
    }

    private Success doResolve(Intent sourceIntent) {
        boolean onlyBrowsers = false;
        filteredItem = null;
        int flag;
        if (SDK_INT >= M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }
        flag = flag | PackageManager.GET_RESOLVED_FILTER;

        List<ResolveInfo> currentResolveList = new ArrayList<>(packageManager.queryIntentActivities(sourceIntent, flag));
        if (Intents.isHttp(sourceIntent) && SDK_INT >= M) {
            List<ResolveInfo> browsers = queryBrowsers(flag);
            addBrowsersToList(currentResolveList, browsers);
            if (browsers.size() == currentResolveList.size()) {
                onlyBrowsers = true;
            }
        }

        //Remove the components from the caller
        if (!TextUtils.isEmpty(callerPackage)) {
            removePackageFromList(callerPackage, currentResolveList);
        }

        final List<DisplayResolveInfo> resolved;
        int size = currentResolveList.size();
        if (size <= 0) {
            resolved = Collections.emptyList();
        } else {
            Collections.sort(currentResolveList, resolverComparator.get());
            resolved = groupResolveList(currentResolveList);
        }
        return new Success(resolved, filteredItem, showExtended, onlyBrowsers);
    }

    private static void removePackageFromList(String packageName, List<ResolveInfo> list) {
        List<ResolveInfo> remove = new ArrayList<>();
        for (ResolveInfo info : list) {
            if (info.activityInfo.packageName.equals(packageName)) {
                remove.add(info);
            }
        }
        list.removeAll(remove);
    }

    private void addBrowsersToList(List<ResolveInfo> list, List<ResolveInfo> browsers) {
        final int initialSize = list.size();

        for (ResolveInfo browser : browsers) {
            boolean browserFound = false;

            for (int i = 0; i < initialSize; i++) {
                ResolveInfo info = list.get(i);

                if (DisplayResolveInfo.equals(info, browser)) {
                    browserFound = true;
                    break;
                }
            }

            if (!browserFound) {
                list.add(browser);
            }
        }
    }

    private List<ResolveInfo> queryBrowsers(int flags) {
        return packageManager.queryIntentActivities(BROWSER_INTENT, flags);
    }

    /**
     * Taken from AOSP, don't try to understand what's going on.
     */
    private List<DisplayResolveInfo> groupResolveList(List<ResolveInfo> current) {
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

    abstract static class State {

        static final State LOADING = new Loading();
        static final State IDLE = new Idle();

        abstract void notify(Listener listener);
    }

    private static class Idle extends State {
        @Override
        void notify(Listener listener) {
            // no-op
        }
    }

    private static class Loading extends State {

        @Override
        void notify(Listener listener) {
            listener.onLoading();
        }
    }

    private static class Success extends State {
        final List<DisplayResolveInfo> resolved;
        @Nullable final DisplayResolveInfo filteredItem;
        final boolean showExtended;
        final boolean onlyBrowsers;

        Success(List<DisplayResolveInfo> resolved, @Nullable DisplayResolveInfo filteredItem, boolean showExtended, boolean onlyBrowsers) {
            this.resolved = resolved;
            this.filteredItem = filteredItem;
            this.showExtended = showExtended;
            this.onlyBrowsers = onlyBrowsers;
        }

        boolean hasFilteredItem() {
            return filteredItem != null;
        }

        @Override
        void notify(Listener listener) {
            listener.onIntentResolved(resolved, filteredItem, showExtended);
        }
    }

    interface Listener {

        void onLoading();

        void onIntentResolved(List<DisplayResolveInfo> list, @Nullable DisplayResolveInfo filteredItem, boolean showExtended);

        Listener NO_OP = new Listener() {
            @Override
            public void onLoading() {
                // no-op
            }

            @Override
            public void onIntentResolved(List<DisplayResolveInfo> list, @Nullable DisplayResolveInfo filteredItem, boolean showExtended) {
                // no-op
            }
        };

    }
}
