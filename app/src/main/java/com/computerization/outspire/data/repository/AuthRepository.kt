package com.computerization.outspire.data.repository

import com.computerization.outspire.data.local.SecureCredentialStore
import com.computerization.outspire.data.remote.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class StoredUser(
    val studentId: String,
    val username: String,
)

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val store: SecureCredentialStore,
) {

    private val _isLoggedIn = MutableStateFlow(computeLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    suspend fun login(username: String, password: String): Result<Unit> = runCatching {
        authService.login(username, password)
        _isLoggedIn.value = true
    }

    suspend fun logout() {
        authService.logout()
        _isLoggedIn.value = false
    }

    fun currentUser(): StoredUser? {
        val id = store.studentId ?: return null
        val name = store.username ?: return null
        return StoredUser(studentId = id, username = name)
    }

    private fun computeLoggedIn(): Boolean =
        store.hasCredentials() && !store.studentId.isNullOrBlank()
}
