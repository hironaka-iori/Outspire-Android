package dev.outspire.android.data.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CasActivityTest {
    @Test
    fun derivesTotalsCategoriesAndReflectionState() {
        val activity = CasActivity(
            id = "1",
            title = "Campus event",
            club = "Computerization",
            date = LocalDate.of(2026, 7, 10),
            creativityHours = 1.5,
            serviceHours = 2.0,
            reflection = "I learned how to coordinate the support team.",
        )

        assertEquals(3.5, activity.hours, 0.001)
        assertEquals(setOf(CasCategory.CREATIVITY, CasCategory.SERVICE), activity.categories)
        assertTrue(activity.reflectionComplete)
        assertFalse(CasCategory.ACTIVITY in activity.categories)
    }
}
