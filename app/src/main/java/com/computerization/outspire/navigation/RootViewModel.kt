package com.computerization.outspire.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.computerization.outspire.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isLoggedIn.value,
        )
}
