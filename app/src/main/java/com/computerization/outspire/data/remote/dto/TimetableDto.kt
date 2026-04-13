package com.computerization.outspire.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableDto(
    @SerialName("WeekList") val weekList: List<WeekEntry> = emptyList(),
    @SerialName("TimetableList") val periods: List<PeriodRow> = emptyList(),
)

@Serializable
data class WeekEntry(
    @SerialName("WeekNumber") val weekNumber: Int = 0,
    @SerialName("WeekName") val weekName: String = "",
)

@Serializable
data class PeriodRow(
    @SerialName("LessonNumber") val lessonNumber: String = "",
    @SerialName("TimetableList") val slots: List<LessonSlot?> = emptyList(),
)

@Serializable
data class LessonSlot(
    @SerialName("SubjectName") val subject: String = "",
    @SerialName("TeacherName") val teacher: String = "",
    @SerialName("ClassRoomNo") val room: String = "",
    @SerialName("LessonNumber") val lessonNumber: Int = 0,
    @SerialName("WeekNumber") val weekNumber: Int = 0,
)
