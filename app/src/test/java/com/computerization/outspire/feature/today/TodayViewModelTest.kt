package com.computerization.outspire.feature.today

import com.computerization.outspire.data.model.DomainClass
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayViewModelTest {

    private val classes = listOf(
        DomainClass("English", "Ms. C", "A1", LocalTime(9, 0), LocalTime(9, 45)),
        DomainClass("Math", "Mr. L", "B1", LocalTime(10, 0), LocalTime(10, 45)),
    )

    private fun at(h: Int, m: Int) = LocalDateTime(
        LocalDate(2026, 4, 12),
        LocalTime(h, m),
    ).toInstant(TimeZone.currentSystemDefault())

    @Test
    fun `in class shows current with remaining`() {
        val state = TodayViewModel.computeState(at(9, 30), classes)
        assertTrue(state is TodayUiState.InClass)
        state as TodayUiState.InClass
        assertEquals("English", state.current.subject)
        assertEquals(15 * 60L, state.remaining.inWholeSeconds)
    }

    @Test
    fun `break shows next with time until`() {
        val state = TodayViewModel.computeState(at(9, 50), classes)
        assertTrue(state is TodayUiState.Break)
        state as TodayUiState.Break
        assertEquals("Math", state.next.subject)
        assertEquals(10 * 60L, state.until.inWholeSeconds)
    }

    @Test
    fun `after last class is Done`() {
        val state = TodayViewModel.computeState(at(23, 0), classes)
        assertTrue(state is TodayUiState.Done)
    }

    @Test
    fun `before first class is a break`() {
        val state = TodayViewModel.computeState(at(7, 0), classes)
        assertTrue(state is TodayUiState.Break)
    }
}
