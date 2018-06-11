package com.tasomaniac.openwith;

import com.tasomaniac.openwith.data.Analytics;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    static Analytics provideAnalytics() {
        return new Analytics.DebugAnalytics();
    }

}
