package com.tasomaniac.openwith;

public class App extends BaseApp {

    @Override
    protected Analytics provideAnalytics() {
        return new Analytics() {
            @Override
            public void sendScreenView(String screenName) {
            }

            @Override
            public void sendEvent(String category, String action, String label, long value) {
            }

            @Override
            public void sendEvent(String category, String action, String label) {
            }
        };
    }
}
