package com.tasomaniac.openwith;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

class AnalyticsImpl implements Analytics {
    private final Tracker tracker;
    private final Answers answers;

    AnalyticsImpl(Tracker tracker) {
        this.tracker = tracker;
        answers = Answers.getInstance();
    }

    @Override
    public void sendScreenView(String screenName) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());

        answers.logContentView(new ContentViewEvent().putContentName(screenName));
    }

    @Override
    public void sendEvent(String category, String action, String label, long value) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());

        answers.logCustom(new CustomEvent(category)
                .putCustomAttribute(action, label)
                .putCustomAttribute("value", value));
    }

    @Override
    public void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, 0);
    }
}
