package com.tasomaniac.openwith.resolver;

import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import com.tasomaniac.openwith.PerActivity;
import dagger.Module;
import dagger.Provides;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static com.tasomaniac.openwith.resolver.ResolverActivity.EXTRA_ADD_TO_HOME_SCREEN;

@Module
public abstract class ResolverModule {

    private static final long USAGE_STATS_PERIOD = TimeUnit.DAYS.toMillis(14);

    @Provides
    @PerActivity
    static ChooserHistory provideChooserHistory(Application app) {
        return ChooserHistory.fromSettings(app);
    }

    @Provides
    static ResolverPresenter resolverPresenter(
            Intent sourceIntent,
            Provider<HomeScreenResolverPresenter> homeScreenResolverPresenterProvider,
            Provider<DefaultResolverPresenter> defaultResolverPresenterProvider) {
        boolean isAddToHomeScreen = sourceIntent.getBooleanExtra(EXTRA_ADD_TO_HOME_SCREEN, false);
        if (isAddToHomeScreen) {
            return homeScreenResolverPresenterProvider.get();
        }
        return defaultResolverPresenterProvider.get();
    }

    @Provides
    static ResolverComparator provideResolverComparator(Application app, ChooserHistory history, Intent sourceIntent) {
        return new ResolverComparator(
                app.getPackageManager(),
                history,
                usageStatsFrom(app),
                new HashSet<>(Arrays.asList(PRIORITY_PACKAGES)),
                sourceIntent
        );
    }

    @Nullable private static Map<String, UsageStats> usageStatsFrom(Context context) {
        if (SDK_INT >= LOLLIPOP_MR1) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            final long sinceTime = System.currentTimeMillis() - USAGE_STATS_PERIOD;
            if (usageStatsManager != null) {
                return usageStatsManager.queryAndAggregateUsageStats(sinceTime, System.currentTimeMillis());
            }
        }
        return null;
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
