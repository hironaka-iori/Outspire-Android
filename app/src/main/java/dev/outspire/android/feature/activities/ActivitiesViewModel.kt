package dev.outspire.android.feature.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.repository.OutspireRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivitiesUiState(
    val isLoading: Boolean = false,
    val activities: List<CasActivity> = emptyList(),
    val error: String? = null,
)

class ActivitiesViewModel(
    private val repository: OutspireRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ActivitiesUiState())
    val state: StateFlow<ActivitiesUiState> = mutableState.asStateFlow()

    fun load() {
        if (mutableState.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            repository.loadActivities()
                .onSuccess { data -> mutableState.value = ActivitiesUiState(activities = data) }
                .onFailure { failure ->
                    mutableState.update {
                        it.copy(isLoading = false, error = failure.message ?: "Unable to load CAS records.")
                    }
                }
        }
    }
}
