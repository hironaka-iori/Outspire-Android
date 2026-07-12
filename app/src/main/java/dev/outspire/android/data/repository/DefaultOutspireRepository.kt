package dev.outspire.android.data.repository

import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SemesterOption
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
    private val cachedSchedules = mutableMapOf<String, List<ScheduleEntry>>()

    override suspend fun login(code: String, password: String): Result<User> {
        if (code.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Enter both your student code and password."))
        }
        cachedSchedules.clear()
        mutableSession.value = null
        return client.login(code.trim(), password).onSuccess { mutableSession.value = it }
    }

    override suspend fun logout() {
        try {
            client.logout()
        } finally {
            cachedSchedules.clear()
            mutableSession.value = null
        }
    }

    override suspend fun loadSchedule(
        forceRefresh: Boolean,
        semesterId: String?,
    ): Result<List<ScheduleEntry>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view your timetable."))
        val cacheKey = semesterId ?: CURRENT_SEMESTER
        if (!forceRefresh) cachedSchedules[cacheKey]?.let { return Result.success(it) }
        val result = client.loadTimetable(user, semesterId)
        result.onSuccess { cachedSchedules[cacheKey] = it }
        return result
    }

    override suspend fun loadSemesters(): Result<List<SemesterOption>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view semesters."))
        return client.loadSemesters(user)
    }

    override suspend fun loadScores(): Result<List<SubjectScore>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view scores."))
        return Result.failure(NotImplementedError("Live score migration is scheduled for the next milestone."))
    }

    override suspend fun loadActivities(): Result<List<CasActivity>> {
        val user = session.value ?: return Result.failure(IllegalStateException("Sign in to view activities."))
        return client.loadActivities(user)
    }

    private companion object {
        const val CURRENT_SEMESTER = "current"
    }
}
