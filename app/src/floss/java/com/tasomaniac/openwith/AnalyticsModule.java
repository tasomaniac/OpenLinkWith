package com.tasomaniac.openwith;

import com.tasomaniac.openwith.data.Analytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    static Analytics provideAnalytics() {
        return new Analytics.DebugAnalytics();
    }

}
