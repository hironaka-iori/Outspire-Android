package com.computerization.outspire.data.repository

import com.computerization.outspire.data.remote.dto.LessonSlot
import com.computerization.outspire.data.remote.dto.PeriodRow
import com.computerization.outspire.data.remote.dto.TimetableDto
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimetableMapperTest {

    private fun slot(subject: String, lesson: Int) =
        LessonSlot(subject = subject, teacher = "T", room = "R", lessonNumber = lesson, weekNumber = 1)

    private val dto = TimetableDto(
        weekList = emptyList(),
        periods = listOf(
            PeriodRow("1", listOf(slot("Math", 1), null, slot("Eng", 1), null, null)),
            PeriodRow("2", listOf(null, slot("Phys", 2), null, null, null)),
            PeriodRow("3", listOf(slot("Chem", 3), slot("Hist", 3), null, null, null)),
        ),
    )

    @Test
    fun `day index 0 picks only monday slots in period order with times`() {
        val out = TimetableRepository.mapDay(dto, 0)
        assertEquals(listOf("Math", "Chem"), out.map { it.subject })
        assertEquals(LocalTime(8, 15), out[0].start)
        assertEquals(LocalTime(8, 55), out[0].end)
        assertEquals(LocalTime(9, 55), out[1].start)
    }

    @Test
    fun `day index 2 picks wednesday slots`() {
        val out = TimetableRepository.mapDay(dto, 2)
        assertEquals(listOf("Eng"), out.map { it.subject })
    }

    @Test
    fun `empty day returns empty list`() {
        val out = TimetableRepository.mapDay(dto, 3)
        assertEquals(emptyList<String>(), out.map { it.subject })
    }
}
