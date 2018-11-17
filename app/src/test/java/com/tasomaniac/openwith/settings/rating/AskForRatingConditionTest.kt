package com.tasomaniac.openwith.settings.rating

import androidx.core.content.edit
import com.tasomaniac.openwith.TestSharedPreferences
import com.tasomaniac.openwith.settings.rating.AskForRatingCondition.Companion.daysAgo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AskForRatingConditionTest {

    private val prefs = TestSharedPreferences()
    private val condition = AskForRatingCondition(prefs)

    @Test
    fun `by default, should not display`() {
        assertThat(condition.shouldDisplay()).isFalse()
    }

    @Test
    fun `given first launch is 3 days ago and app started 5 times, should display`() {
        prefs.edit {
            putLong(PREF_KEY_FIRST_LAUNCH, 3.daysAgo())
        }
        repeat(5) {
            condition.notifyAppLaunch()
        }

        assertThat(condition.shouldDisplay()).isTrue()
    }

    @Test
    fun `given first launch is 2 days ago and app started 5 times, should NOT display`() {
        prefs.edit {
            putLong(PREF_KEY_FIRST_LAUNCH, 2.daysAgo())
        }
        repeat(5) {
            condition.notifyAppLaunch()
        }

        assertThat(condition.shouldDisplay()).isFalse()
    }

    @Test
    fun `given first launch is 3 days ago and app started 4 times, should NOT display`() {
        prefs.edit {
            putLong(PREF_KEY_FIRST_LAUNCH, 3.daysAgo())
        }
        repeat(4) {
            condition.notifyAppLaunch()
        }

        assertThat(condition.shouldDisplay()).isFalse()
    }

    @Test
    fun `given all conditions are met and is already shown, should NOT display`() {
        prefs.edit {
            putLong(PREF_KEY_FIRST_LAUNCH, 3.daysAgo())
        }
        repeat(5) {
            condition.notifyAppLaunch()
        }
        condition.alreadyShown = true

        assertThat(condition.shouldDisplay()).isFalse()
    }

    companion object {

        private const val PREF_KEY_FIRST_LAUNCH = "firstLaunch"
    }
}
