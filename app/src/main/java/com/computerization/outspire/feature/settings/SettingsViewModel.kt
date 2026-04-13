package com.computerization.outspire.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.computerization.outspire.data.remote.dto.YearOption
import com.computerization.outspire.data.repository.AuthRepository
import com.computerization.outspire.data.repository.StoredUser
import com.computerization.outspire.data.repository.YearRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val user: StoredUser? = null,
    val yearOptions: List<YearOption> = emptyList(),
    val currentYearId: String? = null,
    val loggingOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val yearRepository: YearRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(
            user = authRepository.currentUser(),
            yearOptions = yearRepository.cachedOptions().orEmpty(),
            currentYearId = yearRepository.currentYearId.value,
        )
        viewModelScope.launch {
            yearRepository.ensureOptions().onSuccess { options ->
                _state.value = _state.value.copy(
                    yearOptions = options,
                    currentYearId = yearRepository.currentYearId.value,
                )
            }
        }
    }

    fun selectYear(id: String) {
        yearRepository.setCurrentYearId(id)
        _state.value = _state.value.copy(currentYearId = id)
    }

    fun logout() {
        if (_state.value.loggingOut) return
        _state.value = _state.value.copy(loggingOut = true)
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
