package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import com.tasomaniac.openwith.util.ActivityInfoExtensionsKt;
import com.tasomaniac.openwith.util.Intents;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

class IntentResolver {

    private final PackageManager packageManager;
    private final SchedulingStrategy schedulingStrategy;
    private final ResolveListGrouper resolveListGrouper;
    private final Intent sourceIntent;
    private final CallerPackage callerPackage;

    @Nullable private ComponentName lastChosenComponent;
    @Nullable private IntentResolverResult result;
    private Listener listener = Listener.NO_OP;
    private Disposable disposable;

    @Inject
    IntentResolver(PackageManager packageManager,
                   SchedulingStrategy schedulingStrategy,
                   Intent sourceIntent,
                   CallerPackage callerPackage,
                   ResolveListGrouper resolveListGrouper) {
        this.packageManager = packageManager;
        this.schedulingStrategy = schedulingStrategy;
        this.sourceIntent = sourceIntent;
        this.callerPackage = callerPackage;
        this.resolveListGrouper = resolveListGrouper;
    }

    void bind(Listener listener) {
        this.listener = listener;

        if (result == null) {
            resolve();
        } else {
            listener.onIntentResolved(result);
        }
    }

    public void unbind() {
        this.listener = Listener.NO_OP;
    }

    Intent getSourceIntent() {
        return sourceIntent;
    }

    void resolve() {
        disposable = Observable.fromCallable(this::doResolve)
                .compose(schedulingStrategy.forObservable())
                .subscribe(data -> {
                    this.result = data;
                    listener.onIntentResolved(data);
                });
    }

    void release() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private IntentResolverResult doResolve() {
        int flag = SDK_INT >= M ? PackageManager.MATCH_ALL : PackageManager.MATCH_DEFAULT_ONLY;
        List<ResolveInfo> currentResolveList = new ArrayList<>(packageManager.queryIntentActivities(sourceIntent, flag));
        if (Intents.isHttp(sourceIntent) && SDK_INT >= M) {
            addBrowsersToList(currentResolveList);
        }

        callerPackage.removeFrom(currentResolveList);

        List<DisplayActivityInfo> resolved = groupResolveList(currentResolveList);
        return new IntentResolverResult(resolved, resolveListGrouper.filteredItem, resolveListGrouper.showExtended);
    }

    private List<DisplayActivityInfo> groupResolveList(List<ResolveInfo> currentResolveList) {
        if (currentResolveList.isEmpty()) {
            return Collections.emptyList();
        }
        return resolveListGrouper.groupResolveList(currentResolveList, lastChosenComponent);
    }

    private void addBrowsersToList(List<ResolveInfo> list) {
        final int initialSize = list.size();

        for (ResolveInfo browser : queryBrowsers()) {
            boolean browserFound = false;

            for (int i = 0; i < initialSize; i++) {
                ActivityInfo info = list.get(i).activityInfo;

                if (ActivityInfoExtensionsKt.isEqualTo(info, browser.activityInfo)) {
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

    public void setLastChosenComponent(@Nullable ComponentName lastChosenComponent) {
        this.lastChosenComponent = lastChosenComponent;
    }

    interface Listener {

        void onIntentResolved(IntentResolverResult result);

        Listener NO_OP = result -> {
            // no-op
        };

    }
}
