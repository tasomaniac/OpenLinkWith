package com.tasomaniac.openwith;

import android.app.Activity;
import android.os.Bundle;

import com.tasomaniac.openwith.data.Analytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class AnalyticsModule {

    @Provides
    @Singleton
    static Analytics provideAnalytics(App app) {
        if (BuildConfig.DEBUG) {
            return new Analytics.DebugAnalytics();
        }
        return new FirebaseAnalytics(com.google.firebase.analytics.FirebaseAnalytics.getInstance(app));
    }

    private static class FirebaseAnalytics implements Analytics {

        private final com.google.firebase.analytics.FirebaseAnalytics analytics;

        FirebaseAnalytics(com.google.firebase.analytics.FirebaseAnalytics analytics) {
            this.analytics = analytics;
        }

        @Override
        public void sendScreenView(Activity activity, String screenName) {
            analytics.setCurrentScreen(activity, screenName, null);
        }

        @Override
        public void sendEvent(String category, String action, String label) {
            Bundle bundle = new Bundle();
            bundle.putString(action, label);
            analytics.logEvent(category, bundle);
        }
    }

    private AnalyticsModule() {
        //no instance
    }
}
