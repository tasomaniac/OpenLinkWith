package com.tasomaniac.openwith;

import com.tasomaniac.openwith.settings.NightModePreferences;

import javax.inject.Inject;

import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class App extends DaggerApplication {

    private AppComponent component;

    @Inject NightModePreferences nightModePreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        nightModePreferences.updateDefaultNightMode();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    protected AppComponent applicationInjector() {
        component = (AppComponent) DaggerAppComponent.builder().create(this);
        return component;
    }

    public AppComponent component() {
        return component;
    }
}
