package com.tasomaniac.openwith.settings

import android.content.res.Resources
import androidx.annotation.StringRes

interface PreferenceEntries {
    @get:StringRes
    val value: Int
    @get:StringRes
    val entry: Int

    fun stringValue(resources: Resources) = resources.getString(value)

    companion object {

        inline fun <reified T> fromValue(resources: Resources, value: String?): T?
                where T : Enum<T>, T : PreferenceEntries {
            return enumValues<T>()
                .firstOrNull { it.stringValue(resources) == value }
        }
    }
}
