package com.tasomaniac.openwith.settings

import android.app.Application
import android.content.ClipboardManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.core.content.getSystemService
import com.tasomaniac.openwith.BuildConfig
import com.tasomaniac.openwith.settings.advanced.AdvancedSettings
import com.tasomaniac.openwith.settings.advanced.DisableFeaturesSettings
import com.tasomaniac.openwith.settings.advanced.usage.UsageAccessSettings
import com.tasomaniac.openwith.settings.other.OtherSettings
import com.tasomaniac.openwith.settings.rating.AskForRatingSettings
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet

@Module
class SettingsModule {

    @Provides
    fun clipboardManager(app: Application): ClipboardManager = app.getSystemService()!!

    @Provides
    @ElementsIntoSet
    @Suppress("LongParameterList")
    fun settings(
        clipboard: ClipboardSettings,
        askForRating: AskForRatingSettings,
        general: GeneralSettings,
        display: DisplaySettings,
        advanced: AdvancedSettings,
        usageAccess: UsageAccessSettings,
        other: OtherSettings
    ): Set<Settings> = setOf(clipboard, askForRating, general, display, advanced, usageAccess, other)

    @Provides
    @ElementsIntoSet
    fun disableFeaturesSettings(settings: DisableFeaturesSettings): Set<Settings> =
        if (SDK_INT >= M) setOf(settings) else emptySet()

    @Provides
    @ElementsIntoSet
    fun debugSettings(settings: DebugSettings): Set<Settings> =
        if (BuildConfig.DEBUG) setOf(settings) else emptySet()
}
