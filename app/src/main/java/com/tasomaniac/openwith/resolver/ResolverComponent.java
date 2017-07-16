package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.PerActivity;

import dagger.BindsInstance;
import dagger.MembersInjector;
import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = ResolverModule.class)
public interface ResolverComponent extends MembersInjector<ResolverActivity> {

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

