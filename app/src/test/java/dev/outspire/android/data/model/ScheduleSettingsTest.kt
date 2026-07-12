package dev.outspire.android.data.model

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleSettingsTest {
    private val today = LocalDate.of(2026, 7, 11)

    @Test
    fun holidayWithoutEndDateStaysActive() {
        val settings = ScheduleSettings(holidayEnabled = true, holidayEndDateEnabled = false)

        assertTrue(settings.isHolidayActive(today.plusYears(1)))
    }

    @Test
    fun datedHolidayExpiresAfterItsEndDate() {
        val settings = ScheduleSettings(
            holidayEnabled = true,
            holidayEndDateEnabled = true,
            holidayEndDate = today.plusDays(2),
        )

        assertTrue(settings.isHolidayActive(today.plusDays(2)))
        assertFalse(settings.isHolidayActive(today.plusDays(3)))
    }
}
