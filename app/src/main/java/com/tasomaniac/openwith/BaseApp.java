package com.tasomaniac.openwith;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.tasomaniac.openwith.data.Injector;

import timber.log.Timber;

public abstract class BaseApp extends Application {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        component = DaggerAppComponent.builder()
                .application(this)
                .build();
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return component;
        }
        return super.getSystemService(name);
    }

    public static App getApp(Context context) {
        return (App) context.getApplicationContext();
    }

}
