package com.tasomaniac.openwith.test

import com.tasomaniac.openwith.rx.SchedulingStrategy
import io.reactivex.internal.schedulers.TrampolineScheduler

fun testScheduling() =
    SchedulingStrategy(
        TrampolineScheduler.instance(),
        TrampolineScheduler.instance()
    )
