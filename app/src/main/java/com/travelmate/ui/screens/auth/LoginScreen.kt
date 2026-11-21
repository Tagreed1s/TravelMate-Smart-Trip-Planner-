package com.travelmate.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Route: login. Email and password sign-in with links to Sign Up and Forgot Password. */
@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSignedIn by rememberUpdatedState(onSignedIn)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginEffect.SignedIn -> currentOnSignedIn()
            }
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(LoginEvent.MessageShown)
        }
    }

    LoginContent(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onSignUp = onSignUp,
        onForgotPassword = onForgotPassword,
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val colors = TravelMateTheme.colors
    val enabled = !state.loading
    AuthScaffold(snackbarHostState) {
        AuthHeading(title = "Your journey starts here", subtitle = "Sign in and plan your trip with ease.")
        TMTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = "Email",
            leadingIcon = Icons.Rounded.Email,
            error = state.errors[LoginField.EMAIL],
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        TMTextField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = "Password",
            leadingIcon = Icons.Rounded.Lock,
            isPassword = true,
            error = state.errors[LoginField.PASSWORD],
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEvent(LoginEvent.Submit) }),
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgotPassword, enabled = enabled) {
                Text("Forgot password?", color = colors.secondary)
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        TMPrimaryButton(text = "Log in", onClick = { onEvent(LoginEvent.Submit) }, loading = state.loading)
        AuthSwitchLink(question = "Don't have an account?", action = "Sign up", onClick = onSignUp, enabled = enabled)
    }
}

@ThemePreviews
@Composable
private fun LoginEmptyPreview() = TravelMateTheme {
    LoginContent(LoginUiState(), {}, remember { SnackbarHostState() }, {}, {})
}

@ThemePreviews
@Composable
private fun LoginErrorPreview() = TravelMateTheme {
    LoginContent(
        state = LoginUiState(
            email = "sample@",
            submitted = true,
            errors = mapOf(LoginField.EMAIL to "Enter a valid email", LoginField.PASSWORD to "At least 6 characters"),
        ),
        onEvent = {},
        snackbarHostState = remember { SnackbarHostState() },
        onSignUp = {},
        onForgotPassword = {},
    )
}

@ThemePreviews
@Composable
private fun LoginLoadingPreview() = TravelMateTheme {
    LoginContent(
        state = LoginUiState(email = "sample@example.com", password = "secret1", loading = true),
        onEvent = {},
        snackbarHostState = remember { SnackbarHostState() },
        onSignUp = {},
        onForgotPassword = {},
    )
}
