package com.tasomaniac.openwith.settings

import android.app.Application
import android.content.ClipboardManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import androidx.core.content.getSystemService
import com.tasomaniac.openwith.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet

@Module
class SettingsModule {

    @Provides
    fun clipboardManager(app: Application): ClipboardManager = app.getSystemService()!!

    @Provides
    @ElementsIntoSet
    fun settings(
        clipboard: ClipboardSettings,
        general: GeneralSettings,
        display: DisplaySettings,
        advanced: AdvancedSettings,
        other: OtherSettings
    ): Set<Settings> = setOf(clipboard, general, display, advanced, other)

    @Provides
    @ElementsIntoSet
    fun usageAccessSettings(settings: UsageAccessSettings): Set<Settings> =
        if (SDK_INT >= LOLLIPOP) setOf(settings) else setOf()

    @Provides
    @ElementsIntoSet
    fun debugSettings(settings: DebugSettings): Set<Settings> =
        if (BuildConfig.DEBUG) setOf(settings) else setOf()

}
