package com.tasomaniac.openwith;

import com.tasomaniac.openwith.settings.NightModePreferences;
import com.tasomaniac.openwith.settings.rating.AskForRatingCondition;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

import javax.inject.Inject;

public class App extends DaggerApplication {

    private AppComponent component;

    @Inject NightModePreferences nightModePreferences;
    @Inject AskForRatingCondition askForRatingCondition;

    @Override
    public void onCreate() {
        super.onCreate();
        nightModePreferences.updateDefaultNightMode();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        askForRatingCondition.notifyAppLaunch();
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
