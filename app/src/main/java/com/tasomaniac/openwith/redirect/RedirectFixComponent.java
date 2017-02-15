package com.tasomaniac.openwith.redirect;

import com.tasomaniac.openwith.AppComponent;
import com.tasomaniac.openwith.PerActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = AppComponent.class)
interface RedirectFixComponent {
    void inject(RedirectFixActivity activity);
}
