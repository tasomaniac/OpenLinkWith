package com.tasomaniac.openwith.extensions

import android.content.res.Resources
import android.util.TypedValue

fun Resources.dpToPx(dp: Int): Int {
    return TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics).toInt()
}
