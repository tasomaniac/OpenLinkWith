package com.tasomaniac.openwith.util

import android.content.ComponentName
import android.content.pm.ActivityInfo

fun ActivityInfo.componentName() = ComponentName(applicationInfo.packageName, name)

fun ActivityInfo.isEqualTo(other: ActivityInfo) = componentName() == other.componentName()
