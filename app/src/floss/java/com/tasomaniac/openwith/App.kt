package com.tasomaniac.openwith

import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.tasomaniac.openwith.settings.NightModePreferences
import com.tasomaniac.openwith.settings.rating.AskForRatingCondition
import dagger.android.support.DaggerApplication
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

class App : DaggerApplication() {

    @Inject
    lateinit var nightModePreferences: NightModePreferences

    @Inject
    lateinit var askForRatingCondition: AskForRatingCondition

    override fun onCreate() {
        super.onCreate()
        nightModePreferences.updateDefaultNightMode()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        askForRatingCondition.notifyAppLaunch()
    }

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)

    fun createShortcutWith(): Boolean {
        val shortcut = ShortcutInfoCompat.Builder(this, "com.tasomaniac.openwith.LINK_SHARE_TARGET")
            .setCategories(setOf("com.tasomaniac.openwith.LINK_SHARE_TARGET"))
            .setLongLived(true)
            .setShortLabel(getString(R.string.open_with))
            .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher_main))
            .build()
        return ShortcutManagerCompat.setDynamicShortcuts(this, listOf(shortcut))
    }
}
