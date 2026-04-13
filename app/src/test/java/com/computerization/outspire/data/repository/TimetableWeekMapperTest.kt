package com.computerization.outspire.data.repository

import com.computerization.outspire.data.remote.dto.LessonSlot
import com.computerization.outspire.data.remote.dto.PeriodRow
import com.computerization.outspire.data.remote.dto.TimetableDto
import org.junit.Assert.assertEquals
import org.junit.Test

class TimetableWeekMapperTest {

    private fun slot(subject: String, lesson: Int) =
        LessonSlot(subject = subject, teacher = "T", room = "R", lessonNumber = lesson, weekNumber = 1)

    private val dto = TimetableDto(
        periods = listOf(
            PeriodRow("1", listOf(slot("Math", 1), null, slot("Eng", 1), null, slot("PE", 1))),
            PeriodRow("2", listOf(null, slot("Phys", 2), null, null, null)),
        ),
    )

    @Test
    fun `all five weekday indices populate expected slots`() {
        val grid = (0..4).associateWith { TimetableRepository.mapDay(dto, it) }
        assertEquals(listOf("Math"), grid[0]!!.map { it.subject })
        assertEquals(listOf("Phys"), grid[1]!!.map { it.subject })
        assertEquals(listOf("Eng"), grid[2]!!.map { it.subject })
        assertEquals(emptyList<String>(), grid[3]!!.map { it.subject })
        assertEquals(listOf("PE"), grid[4]!!.map { it.subject })
    }
}
