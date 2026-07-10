package dev.outspire.android.data.remote

import dev.outspire.android.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class TsimsProfileParserTest {
    private val fallback = User(
        id = null,
        code = "fallback-code",
        name = "Fallback User",
        role = "Student",
    )

    @Test
    fun `parses identity fields from profile inputs`() {
        val html = """
            <form>
                <input value="42" name="UserId">
                <input name="UserCode" value="s20238123">
                <input name="FirstName" value="Ada">
                <input value="Lovelace" name="LastName">
            </form>
        """.trimIndent()

        val user = TsimsProfileParser.parse(html, fallback)

        assertEquals(42, user.id)
        assertEquals("s20238123", user.code)
        assertEquals("Ada Lovelace", user.name)
        assertEquals("Student", user.role)
    }

    @Test
    fun `falls back to adjacent table cells and decodes common entities`() {
        val studentCodeLabel = "\u5b66\u53f7"
        val studentNameLabel = "\u59d3\u540d"
        val html = """
            <table>
                <tr><th>$studentCodeLabel</th><td>20239999</td></tr>
                <tr><th>$studentNameLabel</th><td>Lin&nbsp;&amp;&nbsp;Kai</td></tr>
            </table>
        """.trimIndent()

        val user = TsimsProfileParser.parse(html, fallback)

        assertEquals("20239999", user.code)
        assertEquals("Lin & Kai", user.name)
    }

    @Test
    fun `keeps login response values when profile fields are absent`() {
        val user = TsimsProfileParser.parse("<html><body>No profile form</body></html>", fallback)

        assertEquals(fallback, user)
    }
}
