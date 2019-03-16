package com.tasomaniac.openwith.resolver

import androidx.core.content.edit
import com.tasomaniac.openwith.TestSharedPreferences
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChooserHistoryTest {

    private val preferences = TestSharedPreferences()

    @Test
    fun fromSettings() {
        val anySaveString = "key1|5#key2|3"
        save(anySaveString)

        val history = ChooserHistory.fromSettings(preferences)

        assertThat(history.asSaveString).isEqualTo(anySaveString)
    }

    @Test(expected = NumberFormatException::class)
    fun shouldThrowExceptionForInvalid() {
        val anySaveString = "key1|5#key2|a"
        save(anySaveString)

        val history = ChooserHistory.fromSettings(preferences)

        assertThat(history.asSaveString).isEqualTo(anySaveString)
    }

    @Test
    fun shouldOmitInvalidParts() {
        save("key1|5#key2#key3|4")

        val history = ChooserHistory.fromSettings(preferences)

        assertThat(history.asSaveString).isEqualTo("key1|5#key3|4")
    }

    @Test
    fun shouldOmitInvalidLastPart() {
        save("key1|5#key2|5#key3")

        val history = ChooserHistory.fromSettings(preferences)

        assertThat(history.asSaveString).isEqualTo("key1|5#key2|5")
    }

    private fun save(value: String) {
        preferences.edit {
            putString("history", value)
        }
    }

}
