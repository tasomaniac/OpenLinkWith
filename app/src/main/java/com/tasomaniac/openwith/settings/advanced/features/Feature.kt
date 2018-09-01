package com.tasomaniac.openwith.settings.advanced.features

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tasomaniac.openwith.R

enum class Feature(
    @StringRes val titleRes: Int,
    @StringRes val detailsRes: Int,
    @DrawableRes val imageRes: Int,
    val className: String,
    val prefKey: String
) {

    ADD_TO_HOMESCREEN(
        R.string.pref_title_feature_add_to_homescreen,
        R.string.pref_details_feature_add_to_homescreen,
        R.drawable.tutorial_4,
        "com.tasomaniac.openwith.homescreen.AddToHomeScreen",
        "pref_feature_add_to_homescreen"
    ),
    TEXT_SELECTION(
        R.string.pref_title_feature_text_selection,
        R.string.pref_details_feature_text_selection,
        R.drawable.feature_text_selection,
        "com.tasomaniac.openwith.TextSelectionActivity",
        "pref_feature_text_selection"
    ),
    DIRECT_SHARE(
        R.string.pref_title_feature_direct_share,
        R.string.pref_details_feature_direct_share,
        R.drawable.feature_direct_share,
        "com.tasomaniac.openwith.resolver.ResolverChooserTargetService",
        "pref_feature_direct_share"
    )
}

fun String.toFeature() = Feature.values().find { it.prefKey == this }!!
