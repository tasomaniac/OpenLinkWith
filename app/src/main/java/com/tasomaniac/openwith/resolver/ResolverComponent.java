package com.tasomaniac.openwith.resolver;

import com.tasomaniac.openwith.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = ResolverModule.class)
public interface ResolverComponent {
    void inject(ResolverActivity activity);

    void inject(ResolveListAdapter adapter);

    @Subcomponent.Builder
    interface Builder {
        Builder resolverModule(ResolverModule resolverModule);
        ResolverComponent build();
    }
}

