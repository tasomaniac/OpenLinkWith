package com.tasomaniac.openwith;

import com.tasomaniac.openwith.browser.PreferredBrowserActivity;
import com.tasomaniac.openwith.homescreen.AddToHomeScreen;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.resolver.ResolverActivity;
import com.tasomaniac.openwith.resolver.ResolverInputModule;
import com.tasomaniac.openwith.resolver.ResolverModule;
import com.tasomaniac.openwith.settings.SettingsActivity;
import com.tasomaniac.openwith.settings.SettingsFragment;
import com.tasomaniac.openwith.settings.SettingsModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface BindingModule {

    @PerActivity
    @ContributesAndroidInjector(modules = {ResolverModule.class, ResolverInputModule.class})
    ResolverActivity resolverActivity();

    @PerActivity
    @ContributesAndroidInjector
    AddToHomeScreenDialogFragment addToHomeScreenDialogFragment();

    @ContributesAndroidInjector
    AddToHomeScreen addToHomeScreen();

    @ContributesAndroidInjector
    PreferredAppsActivity preferredAppsActivity();

    @ContributesAndroidInjector
    PreferredBrowserActivity preferredBrowserActivity();

    @ContributesAndroidInjector
    SettingsActivity settingsActivity();

    @ContributesAndroidInjector(modules = SettingsModule.class)
    SettingsFragment settingsFragment();

    @ContributesAndroidInjector
    IntroActivity introActivity();

    @ContributesAndroidInjector
    RedirectFixActivity redirectFixActivity();

    @ContributesAndroidInjector
    ShareToOpenWith shareToOpenWith();

}
