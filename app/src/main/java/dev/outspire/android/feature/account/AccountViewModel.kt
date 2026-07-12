package dev.outspire.android.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.outspire.android.data.model.User
import dev.outspire.android.data.repository.CredentialRecorder
import dev.outspire.android.data.repository.OutspireRepository
import dev.outspire.android.data.repository.RecordedCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val code: String = "",
    val password: String = "",
    val rememberCredentials: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class AccountViewModel(
    private val repository: OutspireRepository,
    private val credentialRecorder: CredentialRecorder,
) : ViewModel() {
    val session: StateFlow<User?> = repository.session

    private val mutableState = MutableStateFlow(recordedState())
    val state: StateFlow<AccountUiState> = mutableState.asStateFlow()

    fun setCode(value: String) = mutableState.update { it.copy(code = value, error = null) }
    fun setPassword(value: String) = mutableState.update { it.copy(password = value, error = null) }
    fun setRememberCredentials(value: Boolean) {
        if (!value) credentialRecorder.clear()
        mutableState.update { it.copy(rememberCredentials = value, error = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val current = state.value
        if (current.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            repository.login(current.code, current.password)
                .onSuccess {
                    if (current.rememberCredentials) {
                        runCatching {
                            credentialRecorder.save(
                                RecordedCredentials(current.code.trim(), current.password),
                            )
                        }
                        mutableState.value = current.copy(
                            code = current.code.trim(),
                            isLoading = false,
                            error = null,
                        )
                    } else {
                        credentialRecorder.clear()
                        mutableState.value = AccountUiState()
                    }
                    onSuccess()
                }
                .onFailure { failure ->
                    mutableState.update {
                        it.copy(isLoading = false, error = failure.message ?: "Login failed.")
                    }
                }
        }
    }

    fun logout(onComplete: () -> Unit) {
        if (state.value.isLoading) return
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            repository.logout()
            mutableState.value = recordedState()
            onComplete()
        }
    }

    private fun recordedState(): AccountUiState {
        val credentials = credentialRecorder.load() ?: return AccountUiState()
        return AccountUiState(
            code = credentials.code,
            password = credentials.password,
            rememberCredentials = true,
        )
    }
}
