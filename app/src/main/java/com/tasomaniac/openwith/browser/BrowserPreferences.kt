package com.tasomaniac.openwith.browser

import android.content.ComponentName
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class BrowserPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    var mode: Mode
        set(value) {
            sharedPreferences.edit {
                putString(KEY, value.value)
                if (value is Mode.Browser) {
                    putString(KEY_BROWSER_COMPONENT, value.componentName.flattenToString())
                } else {
                    remove(KEY_BROWSER_COMPONENT)
                }
            }
        }
        get() {
            val value = sharedPreferences.getString(KEY, null)
            return when (value) {
                null, "always_ask" -> Mode.AlwaysAsk
                "none" -> Mode.None
                "browser" -> Mode.Browser(componentName)
                else -> throw IllegalStateException()
            }
        }

    private val componentName: ComponentName
        get() {
            val browserComponent = sharedPreferences.getString(KEY_BROWSER_COMPONENT, null)
            return ComponentName.unflattenFromString(browserComponent)
        }

    sealed class Mode(val value: String) {

        object None : Mode("none")
        object AlwaysAsk : Mode("always_ask")
        data class Browser(val componentName: ComponentName) : Mode("browser")

    }

    companion object {
        private const val KEY = "pref_browser"
        private const val KEY_BROWSER_COMPONENT = "pref_browser_component"
    }

}
