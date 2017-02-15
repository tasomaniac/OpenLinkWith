package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tasomaniac.openwith.rx.SchedulingStrategy;
import com.tasomaniac.openwith.util.Intents;
import com.tasomaniac.openwith.util.ResolverInfos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.Lazy;
import io.reactivex.Observable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

class IntentResolver {

    private final RedirectFixer redirectFixer;
    private final PackageManager packageManager;
    private final Lazy<ResolverComparator> resolverComparator;
    private final SchedulingStrategy schedulingStrategy;
    private final ResolveListGrouper resolveListGrouper;
    private final Intent sourceIntent;
    private final String callerPackage;

    private State state = State.IDLE;
    private Listener listener = Listener.NO_OP;

    IntentResolver(RedirectFixer redirectFixer,
                   PackageManager packageManager,
                   Lazy<ResolverComparator> resolverComparator,
                   SchedulingStrategy schedulingStrategy,
                   Intent sourceIntent,
                   String callerPackage,
                   ResolveListGrouper resolveListGrouper) {
        this.redirectFixer = redirectFixer;
        this.packageManager = packageManager;
        this.resolverComparator = resolverComparator;
        this.schedulingStrategy = schedulingStrategy;
        this.sourceIntent = sourceIntent;
        this.callerPackage = callerPackage;
        this.resolveListGrouper = resolveListGrouper;
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

    void resolve() {
        Observable.just(sourceIntent)
                .map(this::doResolve)
                .flatMap(state -> state.hasOnlyBrowsers
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
        boolean hasOnlyBrowsers = false;
        int flag = SDK_INT >= M ? PackageManager.MATCH_ALL :PackageManager.MATCH_DEFAULT_ONLY;
        List<ResolveInfo> currentResolveList = new ArrayList<>(packageManager.queryIntentActivities(sourceIntent, flag));
        if (Intents.isHttp(sourceIntent) && SDK_INT >= M) {
            List<ResolveInfo> browsers = queryBrowsers();
            addBrowsersToList(currentResolveList, browsers);
            if (browsers.size() == currentResolveList.size()) {
                hasOnlyBrowsers = true;
            }
        }

        //Remove the components from the caller
        if (!TextUtils.isEmpty(callerPackage)) {
            removePackageFromList(callerPackage, currentResolveList);
        }

        List<DisplayResolveInfo> resolved = groupResolveList(currentResolveList);
        return new Success(resolved, resolveListGrouper.filteredItem, resolveListGrouper.showExtended, hasOnlyBrowsers);
    }

    private List<DisplayResolveInfo> groupResolveList(List<ResolveInfo> currentResolveList) {
        int size = currentResolveList.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        Collections.sort(currentResolveList, resolverComparator.get());
        return resolveListGrouper.groupResolveList(currentResolveList);
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

                if (ResolverInfos.equals(info, browser)) {
                    browserFound = true;
                    break;
                }
            }

            if (!browserFound) {
                list.add(browser);
            }
        }
    }

    private List<ResolveInfo> queryBrowsers() {
        Intent browserIntent = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("http:"));
        return packageManager.queryIntentActivities(browserIntent, 0);
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
        final boolean hasOnlyBrowsers;

        Success(List<DisplayResolveInfo> resolved, @Nullable DisplayResolveInfo filteredItem, boolean showExtended, boolean hasOnlyBrowsers) {
            this.resolved = resolved;
            this.filteredItem = filteredItem;
            this.showExtended = showExtended;
            this.hasOnlyBrowsers = hasOnlyBrowsers;
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
