package dev.outspire.android.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.ScheduleSettings
import dev.outspire.android.data.model.SchoolTime
import dev.outspire.android.data.repository.OutspireRepository
import dev.outspire.android.data.repository.ScheduleSettingsStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TodayUiState(
    val isLoading: Boolean = false,
    val schedule: List<ScheduleEntry> = emptyList(),
    val settings: ScheduleSettings = ScheduleSettings(),
    val now: LocalDateTime = SchoolTime.now(),
    val error: String? = null,
)

class TodayViewModel(
    private val repository: OutspireRepository,
    private val settingsStore: ScheduleSettingsStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.session.collect { user ->
                settingsStore.selectAccount(user?.code)
            }
        }
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                mutableState.update { it.copy(settings = settings) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                mutableState.update { it.copy(now = SchoolTime.now()) }
                delay(1_000)
            }
        }
    }

    fun setDayOverride(day: DayOfWeek?) = settingsStore.setDayOverride(day)

    fun setHolidayEnabled(enabled: Boolean) = settingsStore.setHolidayEnabled(enabled)

    fun setHolidayEndDateEnabled(enabled: Boolean) =
        settingsStore.setHolidayEndDateEnabled(enabled)

    fun setHolidayEndDate(date: LocalDate) = settingsStore.setHolidayEndDate(date)

    fun setShowFutureCountdown(enabled: Boolean) =
        settingsStore.setShowFutureCountdown(enabled)

    fun load(forceRefresh: Boolean = false) {
        if (mutableState.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            repository.loadSchedule(forceRefresh)
                .onSuccess { schedule ->
                    mutableState.update { it.copy(isLoading = false, schedule = schedule) }
                }
                .onFailure { failure ->
                    mutableState.update {
                        it.copy(isLoading = false, error = failure.message ?: "Unable to load timetable.")
                    }
                }
        }
    }
}
