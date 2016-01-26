package com.tasomaniac.openwith;

import timber.log.Timber;

public interface Analytics {

    void sendScreenView(String screenName);

    void sendEvent(String category, String action, String label, long value);

    void sendEvent(String category, String action, String label);

    class DebugAnalytics implements Analytics {

        @Override
        public void sendScreenView(String screenName) {
            Timber.tag("Analytics").d("Screen: " + screenName);
        }

        @Override
        public void sendEvent(String category, String action, String label) {
            sendEvent(category, action, label, 0);
        }

        @Override
        public void sendEvent(String category, String action, String label, long value) {
            Timber.tag("Analytics").d("Event recorded:"
                    + "\n\tCategory: " + category
                    + "\n\tAction: " + action
                    + "\n\tLabel: " + label
                    + "\n\tValue: " + value);
        }
    }
}
