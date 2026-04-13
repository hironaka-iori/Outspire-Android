package com.computerization.outspire.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.computerization.outspire.BuildConfig
import com.computerization.outspire.data.mock.MockSession
import com.computerization.outspire.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsernameChange(v: String) { _state.value = _state.value.copy(username = v) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v) }

    fun submit() {
        if (_state.value.loading) return
        _state.value = _state.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val username = _state.value.username.trim()
            val password = _state.value.password

            if (BuildConfig.USE_MOCK_BACKEND) {
                delay(600)
                MockSession.isLoggedIn = true
                MockSession.username = username.ifBlank { "demo" }
                _state.value = _state.value.copy(loading = false, loggedIn = true)
                return@launch
            }

            authRepository.login(username, password)
                .onSuccess {
                    MockSession.isLoggedIn = true
                    MockSession.username = username
                    _state.value = _state.value.copy(loading = false, loggedIn = true)
                }
                .onFailure { t ->
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = t.message ?: "Login failed",
                    )
                }
        }
    }
}
