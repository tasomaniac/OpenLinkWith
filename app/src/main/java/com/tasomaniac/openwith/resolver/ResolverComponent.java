package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.PerActivity;

import dagger.BindsInstance;
import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = ResolverModule.class)
public interface ResolverComponent {
    void inject(ResolverActivity activity);

    void inject(ResolveListAdapter adapter);

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder callerPackage(CallerPackage callerPackage);

        @BindsInstance
        Builder lastChosenComponent(@Nullable ComponentName lastChosenComponent);

        @BindsInstance
        Builder sourceIntent(Intent sourceIntent);

        ResolverComponent build();
    }
}

