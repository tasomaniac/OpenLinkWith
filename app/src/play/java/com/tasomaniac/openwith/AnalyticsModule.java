package com.tasomaniac.openwith;

import android.app.Application;

import com.tasomaniac.openwith.data.Analytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    static Analytics provideAnalytics(Application application) {
        if (BuildConfig.DEBUG) {
            return new Analytics.DebugAnalytics();
        }
        return new FirebaseAnalytics(application);
    }
}
