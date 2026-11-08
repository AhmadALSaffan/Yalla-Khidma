package com.yallakhedma.app.util

/** Wall-clock time in epoch milliseconds. */
fun nowMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
