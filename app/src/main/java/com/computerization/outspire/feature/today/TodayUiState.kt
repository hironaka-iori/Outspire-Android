package com.computerization.outspire.feature.today

import com.computerization.outspire.data.model.DomainClass
import kotlin.time.Duration

sealed interface TodayUiState {
    data object Loading : TodayUiState

    data class InClass(
        val current: DomainClass,
        val remaining: Duration,
    ) : TodayUiState

    data class Break(
        val next: DomainClass,
        val until: Duration,
    ) : TodayUiState

    data class Done(val lastSubject: String?, val isWeekend: Boolean) : TodayUiState

    data class Error(val message: String) : TodayUiState
}
