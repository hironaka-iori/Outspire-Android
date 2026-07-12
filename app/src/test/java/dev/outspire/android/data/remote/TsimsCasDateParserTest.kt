package dev.outspire.android.data.remote

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TsimsCasDateParserTest {
    @Test
    fun parsesSupportedTsimsDates() {
        assertEquals(LocalDate.of(2026, 7, 10), parseCasDate("2026-07-10 09:30:00"))
        assertEquals(LocalDate.of(2026, 7, 10), parseCasDate("2026/7/10"))
        assertEquals(LocalDate.of(2026, 7, 10), parseCasDate("7/10/2026"))
    }

    @Test
    fun returnsNullForMissingOrInvalidDates() {
        assertNull(parseCasDate(""))
        assertNull(parseCasDate("not a date"))
    }
}
