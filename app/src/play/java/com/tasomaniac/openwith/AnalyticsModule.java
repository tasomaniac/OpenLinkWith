package com.tasomaniac.openwith;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.tasomaniac.openwith.data.Analytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    Analytics provideAnalytics() {
        if (BuildConfig.DEBUG) {
            return new Analytics.DebugAnalytics();
        }
        return new AnswersAnalytics();
    }

    private static class AnswersAnalytics implements Analytics {
        private final Answers answers = Answers.getInstance();

        @Override
        public void sendScreenView(String screenName) {
            answers.logContentView(new ContentViewEvent().putContentName(screenName));
        }

        @Override
        public void sendEvent(String category, String action, String label) {
            answers.logCustom(new CustomEvent(category)
                    .putCustomAttribute(action, label));
        }
    }
}
