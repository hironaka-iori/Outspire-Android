package com.computerization.outspire.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun tickerFlow(period: Duration = 1.seconds): Flow<Instant> = flow {
    while (currentCoroutineContext().isActive) {
        emit(Clock.System.now())
        delay(period)
    }
}
