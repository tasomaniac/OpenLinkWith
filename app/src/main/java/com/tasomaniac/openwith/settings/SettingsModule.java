package com.tasomaniac.openwith.settings;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsModule {

    @Provides
    static ClipboardManager clipboardManager(Application application) {
        return (ClipboardManager) application.getSystemService(Context.CLIPBOARD_SERVICE);
    }
}
