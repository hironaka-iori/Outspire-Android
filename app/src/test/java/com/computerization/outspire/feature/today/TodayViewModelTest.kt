package com.computerization.outspire.feature.today

import com.computerization.outspire.data.model.DomainClass
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayViewModelTest {

    private val classes = listOf(
        DomainClass("English", "Ms. C", "A1", LocalTime(9, 0), LocalTime(9, 45)),
        DomainClass("Math", "Mr. L", "B1", LocalTime(10, 0), LocalTime(10, 45)),
    )

    private fun at(h: Int, m: Int) = LocalDateTime(
        LocalDate(2026, 4, 13),
        LocalTime(h, m),
    ).toInstant(TimeZone.currentSystemDefault())

    @Test
    fun `in class returns Weekday with active index`() {
        val state = TodayViewModel.computeState(at(9, 30), classes, dayOfWeek = DayOfWeek.MONDAY)
        assertTrue(state is TodayUiState.Weekday)
        state as TodayUiState.Weekday
        assertEquals(0, state.activeIndex)
    }

    @Test
    fun `break returns Weekday with null active index`() {
        val state = TodayViewModel.computeState(at(9, 50), classes, dayOfWeek = DayOfWeek.MONDAY)
        assertTrue(state is TodayUiState.Weekday)
        assertNull((state as TodayUiState.Weekday).activeIndex)
    }

    @Test
    fun `after last class is DayDone afterSchool`() {
        val state = TodayViewModel.computeState(at(23, 0), classes, dayOfWeek = DayOfWeek.MONDAY)
        assertTrue(state is TodayUiState.DayDone)
        state as TodayUiState.DayDone
        assertTrue(state.isAfterSchool)
        assertTrue(!state.isWeekend)
    }

    @Test
    fun `before first class is Weekday with null active index`() {
        val state = TodayViewModel.computeState(at(7, 0), classes, dayOfWeek = DayOfWeek.MONDAY)
        assertTrue(state is TodayUiState.Weekday)
        assertNull((state as TodayUiState.Weekday).activeIndex)
    }

    @Test
    fun `weekend short-circuits to DayDone`() {
        val state = TodayViewModel.computeState(
            at(10, 0), classes, isWeekend = true, dayOfWeek = DayOfWeek.SATURDAY,
        )
        assertTrue(state is TodayUiState.DayDone)
        assertTrue((state as TodayUiState.DayDone).isWeekend)
    }
}
