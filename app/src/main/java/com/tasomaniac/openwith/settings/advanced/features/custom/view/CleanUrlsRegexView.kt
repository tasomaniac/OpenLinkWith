package com.tasomaniac.openwith.settings.advanced.features.custom.view

import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.extensions.inflater
import com.tasomaniac.openwith.redirect.CleanUrlsPreferences
import javax.inject.Inject

class CleanUrlsRegexView @Inject constructor(
    private val cleanUrlsPreferences: CleanUrlsPreferences
) : FeatureToggleCustomView {

    override fun bindCustomContent(customContent: FrameLayout) {
        customContent.inflater().inflate(R.layout.clean_up_regex, customContent)
        val regexEditText = customContent.findViewById<EditText>(R.id.cleanUrlRegexEditText)
        regexEditText.setText(cleanUrlsPreferences.cleanUpRegex.pattern)
        regexEditText.doAfterTextChanged { text ->
            text?.toString()?.takeIf { it.isNotBlank() }?.toRegex()?.let {
                cleanUrlsPreferences.cleanUpRegex = it
            }
        }
    }
}
