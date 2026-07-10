package dev.outspire.android.data.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleResolverTest {
    private val monday = LocalDate.of(2026, 7, 6)
    private val schedule = listOf(
        ScheduleEntry(DayOfWeek.MONDAY, 1, "Mathematics"),
        ScheduleEntry(DayOfWeek.MONDAY, 3, "Physics"),
    )

    @Test
    fun resolvesActivePeriod() {
        val result = ScheduleResolver.currentOrNext(
            schedule = schedule,
            now = LocalDateTime.of(monday, LocalTime.of(8, 35)),
        )

        requireNotNull(result)
        assertEquals(1, result.period.number)
        assertEquals("Mathematics", result.entry.subject)
        assertTrue(result.active)
        assertEquals(0.5f, result.progress, 0.02f)
    }

    @Test
    fun skipsEmptyFuturePeriods() {
        val result = ScheduleResolver.currentOrNext(
            schedule = schedule,
            now = LocalDateTime.of(monday, LocalTime.of(9, 0)),
        )

        requireNotNull(result)
        assertEquals(3, result.period.number)
        assertFalse(result.active)
    }

    @Test
    fun returnsNullOnWeekend() {
        val result = ScheduleResolver.currentOrNext(
            schedule = schedule,
            now = LocalDateTime.of(2026, 7, 11, 9, 0),
        )

        assertNull(result)
    }

    @Test
    fun fillsMissingPeriodsWithSelfStudy() {
        val result = ScheduleResolver.daySchedule(schedule, DayOfWeek.MONDAY)

        assertEquals(9, result.size)
        assertEquals("Self-Study", result[1].subject)
        assertTrue(result[1].isSelfStudy)
    }

    @Test
    fun buildsTodayTimelineWithPastCurrentAndUpcomingPeriods() {
        val timeline = ScheduleResolver.todayTimeline(
            schedule = listOf(
                ScheduleEntry(DayOfWeek.MONDAY, 1, "English"),
                ScheduleEntry(DayOfWeek.MONDAY, 2, "Self-Study"),
                ScheduleEntry(DayOfWeek.MONDAY, 3, "Mathematics"),
            ),
            now = LocalDateTime.of(monday, LocalTime.of(10, 16)),
        )

        assertTrue(timeline.hasRemainingClasses)
        assertEquals(2, timeline.classCount)
        assertEquals(PeriodPhase.PAST, timeline.periods[0].phase)
        assertEquals(PeriodPhase.PAST, timeline.periods[1].phase)
        assertEquals(PeriodPhase.CURRENT, timeline.periods[2].phase)
        assertEquals(PeriodPhase.UPCOMING, timeline.periods[3].phase)
        assertEquals(0.525f, timeline.periods[2].progress, 0.001f)
        assertEquals(19 * 60L, timeline.periods[2].remainingSeconds)
    }

    @Test
    fun hidesScheduleAtTheEndOfTheNinthPeriod() {
        val timeline = ScheduleResolver.todayTimeline(
            schedule = schedule,
            now = LocalDateTime.of(monday, LocalTime.of(16, 30)),
        )

        assertFalse(timeline.hasRemainingClasses)
    }

    @Test
    fun hidesScheduleWhenTheDayOnlyContainsSelfStudy() {
        val timeline = ScheduleResolver.todayTimeline(
            schedule = listOf(ScheduleEntry(DayOfWeek.MONDAY, 1, "Self Study")),
            now = LocalDateTime.of(monday, LocalTime.of(8, 30)),
        )

        assertEquals(0, timeline.classCount)
        assertFalse(timeline.hasRemainingClasses)
    }

    @Test
    fun hidesScheduleOnWeekend() {
        val timeline = ScheduleResolver.todayTimeline(
            schedule = schedule,
            now = LocalDateTime.of(2026, 7, 11, 9, 0),
        )

        assertTrue(timeline.periods.isEmpty())
        assertFalse(timeline.hasRemainingClasses)
    }
}
