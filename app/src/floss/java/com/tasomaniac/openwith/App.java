package com.tasomaniac.openwith;

public class App extends BaseApp {

    @Override
    public void onCreate() {
        super.onCreate();
        analytics = new Analytics.DebugAnalytics();
    }
}
