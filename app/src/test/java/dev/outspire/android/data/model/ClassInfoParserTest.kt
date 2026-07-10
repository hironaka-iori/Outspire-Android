package dev.outspire.android.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassInfoParserTest {
    @Test
    fun parsesTeacherSubjectAndRoom() {
        val result = ClassInfoParser.parse("Ms Chen<br>Mathematics AA HL<br>A401")

        assertEquals("Ms Chen", result.teacher)
        assertEquals("Mathematics AA HL", result.subject)
        assertEquals("A401", result.room)
        assertFalse(result.isSelfStudy)
    }

    @Test
    fun emptyCellBecomesSelfStudy() {
        val result = ClassInfoParser.parse("  ")

        assertEquals("Self-Study", result.subject)
        assertTrue(result.isSelfStudy)
        assertNull(result.teacher)
        assertNull(result.room)
    }
}
