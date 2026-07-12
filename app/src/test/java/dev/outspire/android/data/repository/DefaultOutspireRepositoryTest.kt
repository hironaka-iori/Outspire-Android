package dev.outspire.android.data.repository

import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SemesterOption
import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.User
import dev.outspire.android.data.remote.TsimsDataSource
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultOutspireRepositoryTest {
    private val signedInUser = User(
        id = 7,
        code = "s20238123",
        name = "Outspire Student",
        role = "Student",
    )

    @Test
    fun `login validates both fields before contacting TSIMS`() = runTest {
        val client = FakeTsimsDataSource(Result.success(signedInUser))
        val repository = DefaultOutspireRepository(client)

        val result = repository.login(" ", "password")

        assertTrue(result.isFailure)
        assertEquals("Enter both your student code and password.", result.exceptionOrNull()?.message)
        assertEquals(0, client.loginCalls)
        assertNull(repository.session.value)
    }

    @Test
    fun `successful login trims code and publishes session`() = runTest {
        val client = FakeTsimsDataSource(Result.success(signedInUser))
        val repository = DefaultOutspireRepository(client)

        val result = repository.login("  s20238123  ", "secret password")

        assertSame(signedInUser, result.getOrThrow())
        assertEquals("s20238123", client.lastCode)
        assertEquals("secret password", client.lastPassword)
        assertSame(signedInUser, repository.session.value)
    }

    @Test
    fun `failed login leaves repository signed out`() = runTest {
        val failure = IllegalStateException("Invalid code or password")
        val client = FakeTsimsDataSource(Result.failure(failure))
        val repository = DefaultOutspireRepository(client)

        val result = repository.login("s20238123", "wrong")

        assertSame(failure, result.exceptionOrNull())
        assertNull(repository.session.value)
    }

    @Test
    fun `logout clears published session and delegates server logout`() = runTest {
        val client = FakeTsimsDataSource(Result.success(signedInUser))
        val repository = DefaultOutspireRepository(client)
        repository.login("s20238123", "secret")

        repository.logout()

        assertEquals(1, client.logoutCalls)
        assertNull(repository.session.value)
    }

    @Test
    fun `live activities are loaded from TSIMS after sign in`() = runTest {
        val activity = CasActivity(
            id = "4",
            title = "Campus support",
            club = "Computerization",
            date = LocalDate.of(2026, 7, 10),
            serviceHours = 2.0,
        )
        val client = FakeTsimsDataSource(
            loginResult = Result.success(signedInUser),
            activityResult = Result.success(listOf(activity)),
        )
        val repository = DefaultOutspireRepository(client)
        repository.login("s20238123", "secret")

        val result = repository.loadActivities()

        assertEquals(listOf(activity), result.getOrThrow())
        assertEquals(1, client.activityCalls)
    }

    private class FakeTsimsDataSource(
        private val loginResult: Result<User>,
        private val activityResult: Result<List<CasActivity>> = Result.success(emptyList()),
    ) : TsimsDataSource {
        override val isConfigured = true
        var loginCalls = 0
        var logoutCalls = 0
        var activityCalls = 0
        var lastCode: String? = null
        var lastPassword: String? = null

        override suspend fun login(code: String, password: String): Result<User> {
            loginCalls += 1
            lastCode = code
            lastPassword = password
            return loginResult
        }

        override suspend fun logout() {
            logoutCalls += 1
        }

        override fun clearSession() = Unit

        override suspend fun loadTimetable(
            user: User,
            semesterId: String?,
        ): Result<List<ScheduleEntry>> =
            Result.success(emptyList())

        override suspend fun loadSemesters(user: User): Result<List<SemesterOption>> =
            Result.success(emptyList())

        override suspend fun loadActivities(user: User): Result<List<CasActivity>> =
            activityResult.also { activityCalls += 1 }
    }
}
