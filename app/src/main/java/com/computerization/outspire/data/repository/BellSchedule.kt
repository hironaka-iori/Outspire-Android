package com.computerization.outspire.data.repository

import kotlinx.datetime.LocalTime

/**
 * Hardcoded WFLA 9-period schedule. The TSIMS timetable API returns only
 * `LessonNumber` (1..9); period start/end times are not in the payload.
 * Mirrors iOS `ClassPeriodsModels.swift`.
 */
object BellSchedule {
    data class Period(val start: LocalTime, val end: LocalTime)

    private val periods: Map<Int, Period> = mapOf(
        1 to Period(LocalTime(8, 15), LocalTime(8, 55)),
        2 to Period(LocalTime(9, 5), LocalTime(9, 45)),
        3 to Period(LocalTime(9, 55), LocalTime(10, 35)),
        4 to Period(LocalTime(10, 45), LocalTime(11, 25)),
        5 to Period(LocalTime(12, 30), LocalTime(13, 10)),
        6 to Period(LocalTime(13, 20), LocalTime(14, 0)),
        7 to Period(LocalTime(14, 10), LocalTime(14, 50)),
        8 to Period(LocalTime(15, 0), LocalTime(15, 40)),
        9 to Period(LocalTime(15, 50), LocalTime(16, 30)),
    )

    operator fun get(lessonNumber: Int): Period? = periods[lessonNumber]
}
