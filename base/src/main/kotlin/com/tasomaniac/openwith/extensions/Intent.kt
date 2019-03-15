package com.tasomaniac.openwith.extensions

import android.content.Intent

fun Intent.isHttp() = "http" == scheme || "https" == scheme
