package dev.outspire.android.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TsimsRedirectPolicyTest {
    @Test
    fun `credential post may legitimately finish at login endpoint`() {
        assertFalse(isUnexpectedLoginRedirect("/Home/Login", "/Home/Login"))
    }

    @Test
    fun `redirect from authenticated endpoint back to login is rejected`() {
        assertTrue(
            isUnexpectedLoginRedirect(
                "/Stu/Timetable/GetTimetableByStudent",
                "/Home/Login",
            ),
        )
    }

    @Test
    fun `login path comparison tolerates case trailing slash and query`() {
        assertFalse(
            isUnexpectedLoginRedirect(
                "/home/login?ReturnUrl=%2F",
                "/Home/Login/",
            ),
        )
    }
}
