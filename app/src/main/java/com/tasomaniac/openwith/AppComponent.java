package com.tasomaniac.openwith;

import android.app.Application;

import com.tasomaniac.openwith.data.Analytics;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.resolver.ResolveListAdapter;
import com.tasomaniac.openwith.settings.SettingsActivity;
import com.tasomaniac.openwith.settings.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        AnalyticsModule.class
}, dependencies = {
        Application.class
})
public interface AppComponent {

    Application app();

    Analytics analytics();

    IconLoader iconLoader();

    void inject(IntroActivity activity);

    void inject(PreferredAppsActivity activity);

    void inject(SettingsActivity activity);

    void inject(SettingsFragment fragment);

    void inject(AddToHomeScreenDialogFragment fragment);

    void inject(ResolveListAdapter adapter);
}
