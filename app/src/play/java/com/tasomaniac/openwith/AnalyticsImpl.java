package com.tasomaniac.openwith;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;

class AnalyticsImpl implements Analytics {
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
