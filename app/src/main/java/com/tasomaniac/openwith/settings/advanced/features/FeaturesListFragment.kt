package com.tasomaniac.openwith.settings.advanced.features

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tasomaniac.openwith.R
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class FeaturesListFragment : PreferenceFragmentCompat(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject lateinit var settings: FeaturesListSettings

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        settings.setup()
    }

    override fun onResume() {
        super.onResume()
        activity!!.setTitle(R.string.pref_title_features)
        settings.resume()
    }

    override fun onPause() {
        settings.pause()
        super.onPause()
    }

    override fun onDestroy() {
        settings.release()
        super.onDestroy()
    }

    override fun getCallbackFragment() = this

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        ToggleFeatureActivity.startWith(activity!!, pref)
        return true
    }

}
