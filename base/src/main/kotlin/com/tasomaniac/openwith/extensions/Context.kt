@file:Suppress("NOTHING_TO_INLINE")

package com.tasomaniac.openwith.extensions

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.restart() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)!!
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
}

inline fun Context.toast(@StringRes res: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, res, duration).show()
