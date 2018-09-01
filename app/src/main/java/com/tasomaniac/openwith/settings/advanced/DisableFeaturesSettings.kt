package com.tasomaniac.openwith.settings.advanced

import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import javax.inject.Inject

@RequiresApi(M)
class DisableFeaturesSettings @Inject constructor(fragment: SettingsFragment) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_disable_features)
    }
}
