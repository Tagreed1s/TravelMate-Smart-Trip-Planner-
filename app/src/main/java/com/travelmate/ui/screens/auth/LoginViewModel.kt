package com.travelmate.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.AuthRepository
import com.travelmate.di.appContainer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginField { EMAIL, PASSWORD }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errors: Map<LoginField, String> = emptyMap(),
    /** True after the first tap on Log in; from then on errors update while typing. */
    val submitted: Boolean = false,
    val loading: Boolean = false,
    /** Snackbar text, for example a Firebase error. */
    val message: String? = null,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object Submit : LoginEvent
    data object MessageShown : LoginEvent
}

sealed interface LoginEffect {
    data object SignedIn : LoginEffect
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> updateForm { it.copy(email = event.value) }
            is LoginEvent.PasswordChanged -> updateForm { it.copy(password = event.value) }
            LoginEvent.Submit -> submit()
            LoginEvent.MessageShown -> _state.update { it.copy(message = null) }
        }
    }

    private fun updateForm(transform: (LoginUiState) -> LoginUiState) {
        _state.update { old ->
            val new = transform(old)
            if (new.submitted) new.copy(errors = validate(new)) else new
        }
    }

    private fun submit() {
        val form = _state.value
        if (form.loading) return
        val errors = validate(form)
        _state.update { it.copy(submitted = true, errors = errors) }
        if (errors.isNotEmpty()) return

        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            val result = authRepository.signIn(form.email, form.password)
            _state.update { it.copy(loading = false) }
            result
                .onSuccess { _effects.send(LoginEffect.SignedIn) }
                .onFailure { t -> _state.update { it.copy(message = ErrorMessages.userMessage(t)) } }
        }
    }

    private fun validate(form: LoginUiState): Map<LoginField, String> = buildMap {
        if (!AuthValidation.isValidEmail(form.email)) put(LoginField.EMAIL, "Enter a valid email")
        if (form.password.length < AuthValidation.MIN_PASSWORD) {
            put(LoginField.PASSWORD, "At least ${AuthValidation.MIN_PASSWORD} characters")
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(appContainer().authRepository) }
        }
    }
}
