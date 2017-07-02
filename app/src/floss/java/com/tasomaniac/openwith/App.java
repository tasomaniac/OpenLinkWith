package com.tasomaniac.openwith;

import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class App extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    public AppComponent applicationInjector() {
        return DaggerAppComponent.builder().build(this);
    }
}
