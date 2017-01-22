package com.tasomaniac.openwith.resolver;

import android.app.Application;

import com.tasomaniac.openwith.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
class ResolverModule {

    @Provides
    @PerActivity
    static ChooserHistory provideChooserHistory(Application app) {
        return ChooserHistory.fromSettings(app);
    }

}
