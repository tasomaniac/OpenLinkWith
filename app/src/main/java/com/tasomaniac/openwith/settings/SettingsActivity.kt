package com.tasomaniac.openwith.settings

import android.app.backup.BackupManager
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.transaction
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.prefs.BooleanPreference
import com.tasomaniac.openwith.data.prefs.TutorialShown
import com.tasomaniac.openwith.intro.IntroActivity
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_settings.toolbar
import javax.inject.Inject

class SettingsActivity
    : DaggerAppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Inject @field:TutorialShown lateinit var tutorialShown: BooleanPreference
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var sharedPreferences: SharedPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!tutorialShown.get()) {
            startActivity(IntroActivity.newIntent(this))
            tutorialShown.set(true)
        }

        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        collapsing_toolbar.title = title

        if (savedInstanceState == null) {
            supportFragmentManager.transaction {
                add(R.id.fragment_container, SettingsFragment.newInstance())
            }

            analytics.sendScreenView("Settings")
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
        supportFragmentManager.transaction {
            val fragment = SettingsFragment.newInstance(pref.key)
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        BackupManager(this).dataChanged()
    }
}
