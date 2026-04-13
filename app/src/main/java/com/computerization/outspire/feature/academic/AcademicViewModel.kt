package com.computerization.outspire.feature.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.computerization.outspire.data.repository.ScoreRepository
import com.computerization.outspire.data.repository.YearRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcademicViewModel @Inject constructor(
    private val yearRepository: YearRepository,
    private val scoreRepository: ScoreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AcademicUiState())
    val state: StateFlow<AcademicUiState> = _state.asStateFlow()

    init {
        loadYearsAndScores()
    }

    fun selectYear(id: String) {
        if (id == _state.value.selectedYearId) return
        yearRepository.setCurrentYearId(id)
        _state.value = _state.value.copy(selectedYearId = id, loading = true, error = null)
        loadScores(id)
    }

    fun retry() = loadYearsAndScores()

    private fun loadYearsAndScores() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            yearRepository.ensureOptions()
                .onSuccess { options ->
                    val selected = yearRepository.currentYearId.value ?: options.firstOrNull()?.id
                    _state.value = _state.value.copy(
                        yearOptions = options,
                        selectedYearId = selected,
                    )
                    if (selected != null) loadScores(selected)
                    else _state.value = _state.value.copy(loading = false, error = "No terms available")
                }
                .onFailure { t ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = t.message ?: "Failed to load terms",
                    )
                }
        }
    }

    private fun loadScores(yearId: String) {
        viewModelScope.launch {
            scoreRepository.scores(yearId)
                .onSuccess { scores ->
                    _state.value = _state.value.copy(
                        loading = false,
                        scores = scores,
                        error = null,
                    )
                }
                .onFailure { t ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = t.message ?: "Failed to load scores",
                    )
                }
        }
    }
}
