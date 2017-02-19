package com.tasomaniac.openwith.data;

import timber.log.Timber;

public interface Analytics {

    void sendScreenView(String screenName);

    void sendEvent(String category, String action, String label);

    class DebugAnalytics implements Analytics {

        @Override
        public void sendScreenView(String screenName) {
            Timber.tag("Analytics").d("Screen: %s", screenName);
        }

        @Override
        public void sendEvent(String category, String action, String label) {
            Timber.tag("Analytics").d("Event recorded:\n\tCategory: %s\n\tAction: %s\n\tLabel: %s", category, action, label);
        }
    }
}
