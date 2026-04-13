package com.computerization.outspire.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.computerization.outspire.BuildConfig
import com.computerization.outspire.data.mock.MockClasstable
import com.computerization.outspire.data.model.DomainClass
import com.computerization.outspire.data.repository.TimetableRepository
import com.computerization.outspire.util.tickerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val classesFlow = MutableStateFlow<List<DomainClass>?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val _weekFlow = MutableStateFlow<Map<DayOfWeek, List<DomainClass>>>(emptyMap())
    val weekFlow: StateFlow<Map<DayOfWeek, List<DomainClass>>> = _weekFlow.asStateFlow()

    init {
        load()
    }

    val state: StateFlow<TodayUiState> = combine(
        tickerFlow(1.seconds),
        classesFlow,
        errorFlow,
    ) { now, classes, error ->
        val dow = now.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek
        val isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
        when {
            error != null && classes == null -> TodayUiState.Error(error)
            classes == null -> TodayUiState.Loading
            classes.isEmpty() -> TodayUiState.Done(null, isWeekend)
            else -> computeState(now, classes, isWeekend)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState.Loading,
    )

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            if (BuildConfig.USE_MOCK_BACKEND) {
                classesFlow.value = MockClasstable.today.map {
                    DomainClass(it.subject, it.teacher, it.room, it.start, it.end)
                }
                _weekFlow.value = emptyMap()
                return@launch
            }
            repository.todayClasses()
                .onSuccess {
                    classesFlow.value = it
                    errorFlow.value = null
                }
                .onFailure { t ->
                    errorFlow.value = t.message ?: "Failed to load timetable"
                }
            repository.weekClasses()
                .onSuccess { _weekFlow.value = it }
                .onFailure { /* ignore; today already reported */ }
        }
    }

    companion object {
        fun computeState(
            now: Instant,
            classes: List<DomainClass>,
            isWeekend: Boolean = false,
        ): TodayUiState {
            val nowLocal = now.toLocalDateTime(TimeZone.currentSystemDefault()).time

            val current = classes.firstOrNull { nowLocal >= it.start && nowLocal < it.end }
            if (current != null) {
                val remaining = (current.end.secondOfDay() - nowLocal.secondOfDay()).seconds
                return TodayUiState.InClass(current, remaining.clampPositive())
            }
            val next = classes.firstOrNull { nowLocal < it.start }
            if (next != null) {
                val until = (next.start.secondOfDay() - nowLocal.secondOfDay()).seconds
                return TodayUiState.Break(next, until.clampPositive())
            }
            return TodayUiState.Done(classes.lastOrNull()?.subject, isWeekend)
        }

        private fun LocalTime.secondOfDay(): Int =
            hour * 3600 + minute * 60 + second

        private fun Duration.clampPositive(): Duration =
            if (this < Duration.ZERO) Duration.ZERO else this
    }
}
