package com.computerization.outspire.data.remote

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class YearOptionParserTest {

    @Test
    fun `parses YearId select`() {
        val html = """
            <html><body>
            <select id="YearId">
              <option value="31">2025-2026(T2)</option>
              <option value="2">2025-2026(T1)</option>
              <option value="30">2024-2025(T2)</option>
            </select>
            </body></html>
        """.trimIndent()
        val result = YearService.parseYearOptions(Jsoup.parse(html))
        assertEquals(3, result.size)
        assertEquals("31", result[0].id)
        assertEquals("2025-2026(T2)", result[0].name)
        assertEquals("30", result[2].id)
    }

    @Test
    fun `falls back to ddlYear selector`() {
        val html = """
            <select id="ddlYear">
              <option value="99">Legacy Term</option>
            </select>
        """.trimIndent()
        val result = YearService.parseYearOptions(Jsoup.parse(html))
        assertEquals(1, result.size)
        assertEquals("99", result[0].id)
    }

    @Test
    fun `skips options with blank value`() {
        val html = """
            <select id="YearId">
              <option value="">-- pick --</option>
              <option value="31">2025-2026(T2)</option>
            </select>
        """.trimIndent()
        val result = YearService.parseYearOptions(Jsoup.parse(html))
        assertEquals(listOf("31"), result.map { it.id })
    }

    @Test
    fun `throws when no select found`() {
        val html = "<html><body><p>nothing</p></body></html>"
        assertThrows(IllegalStateException::class.java) {
            YearService.parseYearOptions(Jsoup.parse(html))
        }
    }
}
