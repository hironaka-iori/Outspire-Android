package dev.outspire.android.data.model

import java.time.DayOfWeek
import java.time.LocalDate

data class ScheduleSettings(
    val dayOverride: DayOfWeek? = null,
    val holidayEnabled: Boolean = false,
    val holidayEndDateEnabled: Boolean = false,
    val holidayEndDate: LocalDate = SchoolTime.now().toLocalDate().plusMonths(1),
    val showFutureCountdown: Boolean = true,
) {
    fun isHolidayActive(date: LocalDate): Boolean =
        holidayEnabled && (!holidayEndDateEnabled || date <= holidayEndDate)
}

data class SemesterOption(
    val id: String,
    val label: String,
)
