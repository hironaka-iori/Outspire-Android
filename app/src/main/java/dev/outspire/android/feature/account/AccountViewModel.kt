package dev.outspire.android.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.outspire.android.data.model.User
import dev.outspire.android.data.repository.OutspireRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val code: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class AccountViewModel(
    private val repository: OutspireRepository,
) : ViewModel() {
    val session: StateFlow<User?> = repository.session

    private val mutableState = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = mutableState.asStateFlow()

    fun setCode(value: String) = mutableState.update { it.copy(code = value, error = null) }
    fun setPassword(value: String) = mutableState.update { it.copy(password = value, error = null) }

    fun login(onSuccess: () -> Unit) {
        val current = state.value
        if (current.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            repository.login(current.code, current.password)
                .onSuccess {
                    mutableState.value = AccountUiState()
                    onSuccess()
                }
                .onFailure { failure ->
                    mutableState.update {
                        it.copy(isLoading = false, error = failure.message ?: "Login failed.")
                    }
                }
        }
    }

    fun enterDemo(onSuccess: () -> Unit) {
        repository.enterDemoMode()
        onSuccess()
    }

    fun logout(onComplete: () -> Unit) {
        if (state.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            repository.logout()
            mutableState.value = AccountUiState()
            onComplete()
        }
    }
}
