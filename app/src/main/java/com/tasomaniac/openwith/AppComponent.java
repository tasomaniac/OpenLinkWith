package com.tasomaniac.openwith;

import com.tasomaniac.openwith.resolver.ResolverBindingModule;
import com.tasomaniac.openwith.resolver.ResolverComponent;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        AnalyticsModule.class,
        BindingModule.class,
        ResolverBindingModule.class
})
public interface AppComponent extends AndroidInjector<App> {

    ResolverComponent.Builder resolverComponentBuilder();
    
    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<App> {

        public final AppComponent build(App instance) {
            seedInstance(instance);
            return build();
        }

        @Override
        public abstract AppComponent build();
    }
}
