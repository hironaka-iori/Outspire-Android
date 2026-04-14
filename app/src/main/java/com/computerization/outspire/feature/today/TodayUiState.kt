package com.computerization.outspire.feature.today

import com.computerization.outspire.data.model.DomainClass
import kotlinx.datetime.LocalTime

sealed interface TodayUiState {
    data object Loading : TodayUiState

    data class Weekday(
        val dayName: String,
        val classes: List<DomainClass>,
        val activeIndex: Int?,
        val now: LocalTime,
    ) : TodayUiState

    data class DayDone(
        val isWeekend: Boolean,
        val isAfterSchool: Boolean,
    ) : TodayUiState

    data class Error(val message: String) : TodayUiState
}
