package com.tasomaniac.openwith.resolver;

import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;

import com.tasomaniac.openwith.PerActivity;
import com.tasomaniac.openwith.rx.SchedulingStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static com.tasomaniac.openwith.resolver.ResolverActivity.EXTRA_ADD_TO_HOME_SCREEN;

@Module
public class ResolverModule {

    private static final long USAGE_STATS_PERIOD = TimeUnit.DAYS.toMillis(14);

    private final Intent sourceIntent;
    @Nullable
    private final ComponentName lastChosenComponent;
    private final String callerPackage;

    ResolverModule(ResolverActivity activity, Intent sourceIntent, @Nullable ComponentName lastChosenComponent) {
        this.sourceIntent = sourceIntent;
        this.lastChosenComponent = lastChosenComponent;
        this.callerPackage = ShareCompat.getCallingPackage(activity);
    }

    @Provides
    Intent intent() {
        return sourceIntent;
    }

    @Provides
    @PerActivity
    static ChooserHistory provideChooserHistory(Application app) {
        return ChooserHistory.fromSettings(app);
    }

    @Provides
    ResolverPresenter resolverPresenter(Application app, Resources resources, ChooserHistory history, IntentResolver intentResolver, ViewState viewState) {
        boolean isAddToHomeScreen = sourceIntent.getBooleanExtra(EXTRA_ADD_TO_HOME_SCREEN, false);
        if (isAddToHomeScreen) {
            return new HomeScreenResolverPresenter(resources, intentResolver);
        }
        return new DefaultResolverPresenter(resources, history, app.getContentResolver(), intentResolver, viewState);
    }

    @Provides
    ResolveListGrouper resolveListGrouper(PackageManager packageManager) {
        return new ResolveListGrouper(packageManager, lastChosenComponent);
    }

    @Provides
    @PerActivity
    IntentResolver intentResolver(PackageManager packageManager, Lazy<ResolverComparator> resolverComparator, SchedulingStrategy schedulingStrategy, ResolveListGrouper resolveListGrouper) {
        return new IntentResolver(packageManager, resolverComparator, schedulingStrategy, sourceIntent, callerPackage, resolveListGrouper);
    }

    @Provides
    ResolverComparator provideResolverComparator(Application app, ChooserHistory history) {
        return new ResolverComparator(
                app.getPackageManager(),
                history,
                usageStatsFrom(app),
                priorityItems(),
                sourceIntent
        );
    }

    private static Map<String, UsageStats> usageStatsFrom(Context context) {
        if (SDK_INT >= LOLLIPOP_MR1) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            final long sinceTime = System.currentTimeMillis() - USAGE_STATS_PERIOD;
            return usageStatsManager.queryAndAggregateUsageStats(sinceTime, System.currentTimeMillis());
        } else {
            return null;
        }
    }

    private static Map<String, Integer> priorityItems() {
        int size = PRIORITY_PACKAGES.length;
        Map<String, Integer> priorityPackages = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            // position 0 should have highest priority,
            // starting with 1 for lowest priority.
            priorityPackages.put(PRIORITY_PACKAGES[i], size - i + 1);
        }
        return priorityPackages;
    }

    private static final String[] PRIORITY_PACKAGES = new String[]{
            "com.whatsapp",
            "com.twitter.android",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.google.android.talk",
            "com.google.android.apps.plus",
            "com.google.android.apps.photos",
            "com.pandora.android",
            "com.instagram.android",
            "com.linkedin.android",
            "com.spotify.music",
            "com.pinterest",
            "com.medium.reader",
            "com.ubercab",
            "com.meetup",
            "com.tumblr",
            "com.badoo.mobile",
            "tv.periscope.android",
            "com.skype.raider"
    };

}
