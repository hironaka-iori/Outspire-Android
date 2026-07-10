package dev.outspire.android.data.repository

import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SubjectScore
import dev.outspire.android.data.model.User
import kotlinx.coroutines.flow.StateFlow

interface OutspireRepository {
    val session: StateFlow<User?>

    suspend fun login(code: String, password: String): Result<User>
    fun enterDemoMode()
    suspend fun logout()
    suspend fun loadSchedule(forceRefresh: Boolean = false): Result<List<ScheduleEntry>>
    suspend fun loadScores(): Result<List<SubjectScore>>
    suspend fun loadActivities(): Result<List<CasActivity>>
}
