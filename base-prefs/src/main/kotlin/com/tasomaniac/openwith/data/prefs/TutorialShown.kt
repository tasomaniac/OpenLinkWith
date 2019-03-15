package com.tasomaniac.openwith.data.prefs

import javax.inject.Qualifier

import java.lang.annotation.RetentionPolicy.RUNTIME
import kotlin.annotation.Retention

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TutorialShown
