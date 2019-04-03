package com.tasomaniac.openwith

import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.DataModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        AnalyticsModule::class,
        DataModule::class,
        BindingModule::class
    ]
)
interface AppComponent : AndroidInjector<App> {

    fun analytics(): Analytics

    @Component.Factory
    interface Factory : AndroidInjector.Factory<App> {
        override fun create(@BindsInstance instance: App): AppComponent
    }
}
