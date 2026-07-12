package dev.outspire.android.data.repository

import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SemesterOption
import dev.outspire.android.data.model.SubjectScore
import dev.outspire.android.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface OutspireRepository {
    val session: StateFlow<User?>

    suspend fun login(code: String, password: String): Result<User>
    suspend fun logout()
    suspend fun loadSchedule(
        forceRefresh: Boolean = false,
        semesterId: String? = null,
    ): Result<List<ScheduleEntry>>
    suspend fun loadSemesters(): Result<List<SemesterOption>>
    suspend fun loadScores(): Result<List<SubjectScore>>
    suspend fun loadActivities(): Result<List<CasActivity>>
}
