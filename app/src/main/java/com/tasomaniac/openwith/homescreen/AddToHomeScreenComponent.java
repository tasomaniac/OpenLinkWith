package com.tasomaniac.openwith.homescreen;

import com.tasomaniac.openwith.AppComponent;
import com.tasomaniac.openwith.PerActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = AppComponent.class)
interface AddToHomeScreenComponent {
    void inject(AddToHomeScreenDialogFragment fragment);
}
