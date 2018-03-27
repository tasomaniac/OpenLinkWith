package com.tasomaniac.openwith

import com.tasomaniac.devwidget.data.DataModule
import com.tasomaniac.openwith.data.Analytics
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

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>() {

        abstract override fun build(): AppComponent
    }
}
