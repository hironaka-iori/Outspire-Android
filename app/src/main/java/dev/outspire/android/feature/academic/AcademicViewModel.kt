package dev.outspire.android.feature.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SchoolTime
import dev.outspire.android.data.model.SubjectScore
import dev.outspire.android.data.repository.OutspireRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AcademicUiState(
    val isLoading: Boolean = false,
    val schedule: List<ScheduleEntry> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val selectedDate: LocalDate = nearestSchoolDate(SchoolTime.now().toLocalDate()),
    val now: LocalDateTime = SchoolTime.now(),
    val scheduleError: String? = null,
    val scoreError: String? = null,
) {
    val selectedDay: DayOfWeek
        get() = selectedDate.dayOfWeek
}

internal fun nearestSchoolDate(date: LocalDate): LocalDate = when (date.dayOfWeek) {
    DayOfWeek.SATURDAY -> date.minusDays(1)
    DayOfWeek.SUNDAY -> date.minusDays(2)
    else -> date
}

class AcademicViewModel(
    private val repository: OutspireRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AcademicUiState())
    val state: StateFlow<AcademicUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                mutableState.update { it.copy(now = SchoolTime.now()) }
                delay(1_000)
            }
        }
    }

    fun selectDay(day: DayOfWeek) {
        val monday = mutableState.value.selectedDate.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY),
        )
        selectDate(monday.plusDays(day.value.toLong() - DayOfWeek.MONDAY.value))
    }

    fun selectDate(date: LocalDate) = mutableState.update {
        it.copy(selectedDate = nearestSchoolDate(date))
    }

    fun selectToday() = selectDate(SchoolTime.now().toLocalDate())

    fun load(forceRefresh: Boolean = false) {
        if (mutableState.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, scheduleError = null, scoreError = null) }
            val schedule = async { repository.loadSchedule(forceRefresh) }
            val scores = async { repository.loadScores() }
            val scheduleResult = schedule.await()
            val scoreResult = scores.await()
            mutableState.update { old ->
                old.copy(
                    isLoading = false,
                    schedule = scheduleResult.getOrDefault(old.schedule),
                    scores = scoreResult.getOrDefault(old.scores),
                    scheduleError = scheduleResult.exceptionOrNull()?.message,
                    scoreError = scoreResult.exceptionOrNull()?.message,
                )
            }
        }
    }
}
