package com.tasomaniac.openwith.test

import com.nhaarman.mockitokotlin2.KStubbing
import com.nhaarman.mockitokotlin2.doReturn
import org.mockito.stubbing.OngoingStubbing

fun <T, R> KStubbing<T>.given(methodCall: T.() -> R) = on(methodCall)
infix fun <T> OngoingStubbing<T>.willReturn(t: T) = doReturn(t)
fun <T> OngoingStubbing<T>.willReturn(t: T, vararg ts: T) = doReturn(t, *ts)
