package com.tasomaniac.openwith.settings.advanced.features

import androidx.annotation.StringRes
import com.tasomaniac.openwith.R

enum class Feature(
    @StringRes val titleRes: Int,
    @StringRes val detailsRes: Int,
    val prefKey: String
) {

    ADD_TO_HOMESCREEN(
        R.string.pref_title_feature_add_to_homescreen,
        R.string.pref_details_feature_add_to_homescreen,
        "pref_feature_add_to_homescreen"
    ),
    TEXT_SELECTION(
        R.string.pref_title_feature_text_selection,
        R.string.pref_details_feature_text_selection,
        "pref_feature_text_selection"
    ),
    DIRECT_SHARE(
        R.string.pref_title_feature_direct_share,
        R.string.pref_details_feature_direct_share,
        "pref_feature_direct_share"
    )

}

fun String.toFeature() = Feature.values().find { it.prefKey == this }!!

