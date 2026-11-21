package com.travelmate.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Route: sign_up. Full name, email, password and confirmation; opens Explore when the account exists. */
@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onLogIn: () -> Unit,
    viewModel: SignUpViewModel = viewModel(factory = SignUpViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSignedUp by rememberUpdatedState(onSignedUp)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SignUpEffect.SignedUp -> currentOnSignedUp()
            }
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SignUpEvent.MessageShown)
        }
    }

    SignUpContent(state = state, onEvent = viewModel::onEvent, snackbarHostState = snackbarHostState, onLogIn = onLogIn)
}

@Composable
fun SignUpContent(
    state: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onLogIn: () -> Unit,
) {
    val enabled = !state.loading
    AuthScaffold(snackbarHostState) {
        AuthHeading(title = "Create your account", subtitle = "Join TravelMate and start planning.")
        TMTextField(
            value = state.name,
            onValueChange = { onEvent(SignUpEvent.NameChanged(it)) },
            label = "Full name",
            leadingIcon = Icons.Rounded.Person,
            error = state.errors[SignUpField.NAME],
            enabled = enabled,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        TMTextField(
            value = state.email,
            onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
            label = "Email",
            leadingIcon = Icons.Rounded.Email,
            error = state.errors[SignUpField.EMAIL],
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        TMTextField(
            value = state.password,
            onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
            label = "Password",
            leadingIcon = Icons.Rounded.Lock,
            isPassword = true,
            error = state.errors[SignUpField.PASSWORD],
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        )
        TMTextField(
            value = state.confirm,
            onValueChange = { onEvent(SignUpEvent.ConfirmChanged(it)) },
            label = "Confirm password",
            leadingIcon = Icons.Rounded.Lock,
            isPassword = true,
            error = state.errors[SignUpField.CONFIRM],
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEvent(SignUpEvent.Submit) }),
        )
        Spacer(Modifier.height(Spacing.sm))
        TMPrimaryButton(text = "Create account", onClick = { onEvent(SignUpEvent.Submit) }, loading = state.loading)
        AuthSwitchLink(question = "Already have an account?", action = "Log in", onClick = onLogIn, enabled = enabled)
    }
}

@ThemePreviews
@Composable
private fun SignUpEmptyPreview() = TravelMateTheme {
    SignUpContent(SignUpUiState(), {}, remember { SnackbarHostState() }, {})
}

@ThemePreviews
@Composable
private fun SignUpErrorPreview() = TravelMateTheme {
    SignUpContent(
        state = SignUpUiState(
            email = "sample@example.com",
            password = "abc",
            confirm = "abd",
            submitted = true,
            errors = mapOf(
                SignUpField.NAME to "Enter your name",
                SignUpField.PASSWORD to "At least 6 characters",
                SignUpField.CONFIRM to "Passwords do not match",
            ),
        ),
        onEvent = {},
        snackbarHostState = remember { SnackbarHostState() },
        onLogIn = {},
    )
}

@ThemePreviews
@Composable
private fun SignUpLoadingPreview() = TravelMateTheme {
    SignUpContent(
        state = SignUpUiState(name = "SAMPLE User", email = "sample@example.com", password = "secret1", confirm = "secret1", loading = true),
        onEvent = {},
        snackbarHostState = remember { SnackbarHostState() },
        onLogIn = {},
    )
}
