package dev.outspire.android.data.repository

import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SubjectScore
import dev.outspire.android.data.model.User
import dev.outspire.android.data.remote.TsimsDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultOutspireRepository(
    private val client: TsimsDataSource,
) : OutspireRepository {
    private val mutableSession = MutableStateFlow<User?>(null)
    override val session: StateFlow<User?> = mutableSession.asStateFlow()
    private var cachedSchedule: List<ScheduleEntry>? = null

    override suspend fun login(code: String, password: String): Result<User> {
        if (code.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter both your student code and password."))
        }
        cachedSchedule = null
        mutableSession.value = null
        return client.login(code.trim(), password).onSuccess { mutableSession.value = it }
    }

    override fun enterDemoMode() {
        client.clearSession()
        cachedSchedule = null
        mutableSession.value = User(
            id = null,
            code = "demo",
            name = "Outspire Student",
            role = "Student",
            isDemo = true,
        )
    }

    override suspend fun logout() {
        try {
            client.logout()
        } finally {
            cachedSchedule = null
            mutableSession.value = null
        }
    }

    override suspend fun loadSchedule(forceRefresh: Boolean): Result<List<ScheduleEntry>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view your timetable."))
        if (!forceRefresh) cachedSchedule?.let { return Result.success(it) }
        val result = if (user.isDemo) Result.success(DemoData.schedule) else client.loadTimetable(user)
        result.onSuccess { cachedSchedule = it }
        return result
    }

    override suspend fun loadScores(): Result<List<SubjectScore>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view scores."))
        return if (user.isDemo) {
            Result.success(DemoData.scores)
        } else {
            Result.failure(NotImplementedError("Live score migration is scheduled for the next milestone."))
        }
    }

    override suspend fun loadActivities(): Result<List<CasActivity>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view activities."))
        return if (user.isDemo) {
            Result.success(DemoData.activities)
        } else {
            Result.failure(NotImplementedError("Live CAS migration is scheduled for the next milestone."))
        }
    }
}
