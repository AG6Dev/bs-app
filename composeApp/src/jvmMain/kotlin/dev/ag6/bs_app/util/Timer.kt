package dev.ag6.bs_app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun CoroutineScope.scheduleRepeatingTask(
    interval: Duration,
    block: suspend () -> Unit
): Job = launch {
    while(isActive) {
        block()
        delay(interval)
    }
}