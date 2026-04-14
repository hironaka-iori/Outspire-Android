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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val classesFlow = MutableStateFlow<List<DomainClass>?>(null)
    private val errorFlow = MutableStateFlow<String?>(null)

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
            else -> computeState(now, classes, isWeekend, dow)
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
        }
    }

    companion object {
        fun computeState(
            now: Instant,
            classes: List<DomainClass>,
            isWeekend: Boolean = false,
            dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
        ): TodayUiState {
            if (isWeekend) return TodayUiState.DayDone(isWeekend = true, isAfterSchool = false)
            if (classes.isEmpty()) return TodayUiState.DayDone(isWeekend = false, isAfterSchool = false)
            val nowLocal = now.toLocalDateTime(TimeZone.currentSystemDefault()).time
            if (classes.all { nowLocal >= it.end }) {
                return TodayUiState.DayDone(isWeekend = false, isAfterSchool = true)
            }
            val activeIdx = classes.indexOfFirst { nowLocal >= it.start && nowLocal < it.end }
                .takeIf { it >= 0 }
            return TodayUiState.Weekday(
                dayName = dayName(dayOfWeek),
                classes = classes,
                activeIndex = activeIdx,
                now = nowLocal,
            )
        }

        private fun dayName(d: DayOfWeek): String = when (d) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            else -> ""
        }
    }
}
