package com.tasomaniac.openwith;

import com.tasomaniac.openwith.homescreen.AddToHomeScreen;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;
import com.tasomaniac.openwith.intro.IntroActivity;
import com.tasomaniac.openwith.preferred.PreferredAppsActivity;
import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.settings.SettingsActivity;
import com.tasomaniac.openwith.settings.SettingsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
interface BindingModule {

    @PerActivity
    @ContributesAndroidInjector
    AddToHomeScreenDialogFragment addToHomeScreenDialogFragment();

    @ContributesAndroidInjector
    AddToHomeScreen addToHomeScreen();

    @ContributesAndroidInjector
    PreferredAppsActivity preferredAppsActivity();

    @ContributesAndroidInjector
    SettingsActivity settingsActivity();

    @ContributesAndroidInjector
    SettingsFragment settingsFragment();

    @ContributesAndroidInjector
    IntroActivity introActivity();

    @ContributesAndroidInjector
    RedirectFixActivity redirectFixActivity();

    @ContributesAndroidInjector
    ShareToOpenWith shareToOpenWith();
}
