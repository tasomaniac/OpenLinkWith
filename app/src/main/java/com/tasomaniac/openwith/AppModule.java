package com.tasomaniac.openwith;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import com.tasomaniac.openwith.data.prefs.BooleanPreference;
import com.tasomaniac.openwith.data.prefs.TutorialShown;
import com.tasomaniac.openwith.data.prefs.UsageAccess;
import com.tasomaniac.openwith.resolver.IconLoader;
import com.tasomaniac.openwith.rx.SchedulingStrategy;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import javax.inject.Singleton;
import java.io.File;

@Module
abstract class AppModule {
    private static final int DISK_CACHE_SIZE = 5 * 1024 * 1024;

    @Binds
    abstract Application application(App app);

    @Provides
    static PackageManager packageManager(Application app) {
        return app.getPackageManager();
    }

    @Provides
    static SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    static ActivityManager provideActivityManager(Application app) {
        return (ActivityManager) app.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Provides
    static Resources resources(Application app) {
        return app.getResources();
    }
    
    @Provides
    static SchedulingStrategy schedulingStrategy() {
        return new SchedulingStrategy(Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @Provides
    static IconLoader provideIconLoader(PackageManager pm, ActivityManager am) {
        int iconDpi = am.getLauncherLargeIconDensity();
        return new IconLoader(pm, iconDpi);
    }

    @Provides
    @Singleton
    @TutorialShown
    static BooleanPreference provideTutorialShown(SharedPreferences prefs) {
        return new BooleanPreference(prefs, "pref_tutorial_shown");
    }

    @Provides
    @Singleton
    @UsageAccess
    static BooleanPreference provideUsageAccess(SharedPreferences prefs) {
        return new BooleanPreference(prefs, "usage_access");
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(Application app) {
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }
}
