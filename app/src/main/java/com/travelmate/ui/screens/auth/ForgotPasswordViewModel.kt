package com.travelmate.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.data.repository.AuthRepository
import com.travelmate.di.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val error: String? = null,
    val loading: Boolean = false,
    /** True after the request was sent; the screen then shows the "check your email" card. */
    val sent: Boolean = false,
)

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent
    data object Submit : ForgotPasswordEvent
    data object UseAnotherEmail : ForgotPasswordEvent
}

class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> _state.update { it.copy(email = event.value, error = null) }
            ForgotPasswordEvent.Submit -> submit()
            ForgotPasswordEvent.UseAnotherEmail -> _state.update { it.copy(sent = false) }
        }
    }

    private fun submit() {
        val email = _state.value.email.trim()
        if (_state.value.loading) return
        if (!AuthValidation.isValidEmail(email)) {
            _state.update { it.copy(error = "Enter a valid email") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            // Same answer whether or not the account exists, so emails cannot be probed.
            authRepository.sendPasswordReset(email)
            _state.update { it.copy(loading = false, sent = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ForgotPasswordViewModel(appContainer().authRepository) }
        }
    }
}
