package com.tasomaniac.openwith.resolver;

import com.tasomaniac.openwith.AppComponent;
import com.tasomaniac.openwith.PerActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = AppComponent.class, modules = ResolverModule.class)
interface ResolverComponent {
    void inject(ResolverActivity activity);

    void inject(ResolveListAdapter adapter);
}
