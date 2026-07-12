package dev.outspire.android.feature.academic

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class AcademicViewModelTest {
    @Test
    fun keepsWeekdaysSelected() {
        val monday = LocalDate.of(2026, 7, 6)

        assertEquals(monday, nearestSchoolDate(monday))
    }

    @Test
    fun mapsWeekendDatesToFriday() {
        val friday = LocalDate.of(2026, 7, 10)

        assertEquals(friday, nearestSchoolDate(LocalDate.of(2026, 7, 11)))
        assertEquals(friday, nearestSchoolDate(LocalDate.of(2026, 7, 12)))
    }
}
