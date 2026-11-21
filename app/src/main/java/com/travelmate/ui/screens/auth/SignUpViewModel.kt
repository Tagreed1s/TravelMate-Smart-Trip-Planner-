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

enum class SignUpField { NAME, EMAIL, PASSWORD, CONFIRM }

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val errors: Map<SignUpField, String> = emptyMap(),
    /** True after the first tap on Create account; from then on errors update while typing. */
    val submitted: Boolean = false,
    val loading: Boolean = false,
    val message: String? = null,
)

sealed interface SignUpEvent {
    data class NameChanged(val value: String) : SignUpEvent
    data class EmailChanged(val value: String) : SignUpEvent
    data class PasswordChanged(val value: String) : SignUpEvent
    data class ConfirmChanged(val value: String) : SignUpEvent
    data object Submit : SignUpEvent
    data object MessageShown : SignUpEvent
}

sealed interface SignUpEffect {
    data object SignedUp : SignUpEffect
}

/** Creates the Firebase account; the repository also writes /users/{uid}/profile. */
class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    private val _effects = Channel<SignUpEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.NameChanged -> updateForm { it.copy(name = event.value.take(AuthValidation.MAX_NAME)) }
            is SignUpEvent.EmailChanged -> updateForm { it.copy(email = event.value) }
            is SignUpEvent.PasswordChanged -> updateForm { it.copy(password = event.value) }
            is SignUpEvent.ConfirmChanged -> updateForm { it.copy(confirm = event.value) }
            SignUpEvent.Submit -> submit()
            SignUpEvent.MessageShown -> _state.update { it.copy(message = null) }
        }
    }

    private fun updateForm(transform: (SignUpUiState) -> SignUpUiState) {
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
            val result = authRepository.signUp(form.name, form.email, form.password)
            _state.update { it.copy(loading = false) }
            result
                .onSuccess { _effects.send(SignUpEffect.SignedUp) }
                .onFailure { t -> _state.update { it.copy(message = ErrorMessages.userMessage(t)) } }
        }
    }

    private fun validate(form: SignUpUiState): Map<SignUpField, String> = buildMap {
        if (form.name.isBlank()) put(SignUpField.NAME, "Enter your name")
        if (!AuthValidation.isValidEmail(form.email)) put(SignUpField.EMAIL, "Enter a valid email")
        if (form.password.length < AuthValidation.MIN_PASSWORD) {
            put(SignUpField.PASSWORD, "At least ${AuthValidation.MIN_PASSWORD} characters")
        }
        if (form.confirm != form.password) put(SignUpField.CONFIRM, "Passwords do not match")
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SignUpViewModel(appContainer().authRepository) }
        }
    }
}
