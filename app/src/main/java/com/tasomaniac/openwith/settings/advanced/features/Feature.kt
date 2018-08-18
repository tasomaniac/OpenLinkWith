package com.tasomaniac.openwith.settings.advanced.features

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tasomaniac.openwith.R

enum class Feature(
    @StringRes val titleRes: Int,
    @StringRes val detailsRes: Int,
    @DrawableRes val imageRes: Int,
    val prefKey: String
) {

    ADD_TO_HOMESCREEN(
        R.string.pref_title_feature_add_to_homescreen,
        R.string.pref_details_feature_add_to_homescreen,
        R.drawable.tutorial_4,
        "pref_feature_add_to_homescreen"
    ),
    TEXT_SELECTION(
        R.string.pref_title_feature_text_selection,
        R.string.pref_details_feature_text_selection,
        R.drawable.tutorial_4, // TODO add text selection image
        "pref_feature_text_selection"
    ),
    DIRECT_SHARE(
        R.string.pref_title_feature_direct_share,
        R.string.pref_details_feature_direct_share,
        R.drawable.tutorial_4, // TODO add direct share image
        "pref_feature_direct_share"
    )

}

fun String.toFeature() = Feature.values().find { it.prefKey == this }!!

