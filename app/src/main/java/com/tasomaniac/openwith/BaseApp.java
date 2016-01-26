package com.tasomaniac.openwith;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

public abstract class BaseApp extends Application {

    Analytics analytics;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public static App getApp(Context context) {
        return (App) context.getApplicationContext();
    }

    public Analytics getAnalytics() {
        return analytics;
    }

}
