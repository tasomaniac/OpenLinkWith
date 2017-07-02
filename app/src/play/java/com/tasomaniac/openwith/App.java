package com.tasomaniac.openwith;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import dagger.android.support.DaggerApplication;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class App extends DaggerApplication {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashReportingTree());
        } else {
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

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            Crashlytics.log(priority, tag, message);
            if (t != null && priority >= Log.WARN) {
                Crashlytics.logException(t);
            }
        }
    }
}
