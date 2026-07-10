package dev.outspire.android.feature.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SubjectScore
import dev.outspire.android.data.repository.OutspireRepository
import java.time.DayOfWeek
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcademicUiState(
    val isLoading: Boolean = false,
    val schedule: List<ScheduleEntry> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val scheduleError: String? = null,
    val scoreError: String? = null,
)

class AcademicViewModel(
    private val repository: OutspireRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AcademicUiState())
    val state: StateFlow<AcademicUiState> = mutableState.asStateFlow()

    fun selectDay(day: DayOfWeek) = mutableState.update { it.copy(selectedDay = day) }

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
