package com.travelmate.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Route: forgot_password. Sends a Firebase password reset link to the email the user enters. */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(factory = ForgotPasswordViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ForgotPasswordContent(state = state, onEvent = viewModel::onEvent, onBack = onBack)
}

@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    onBack: () -> Unit,
) {
    val colors = TravelMateTheme.colors
    AmbientBackground {
        Column(Modifier.fillMaxSize()) {
            TMTopBar(title = "Reset password", onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                if (state.sent) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        EmptyState(
                            icon = Icons.Rounded.Email,
                            title = "Check your email",
                            message = "If an account exists for ${state.email.trim()}, a reset link is on its way.",
                        )
                    }
                    TMPrimaryButton(text = "Back to log in", onClick = onBack)
                    TextButton(
                        onClick = { onEvent(ForgotPasswordEvent.UseAnotherEmail) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Use another email", color = colors.secondary)
                    }
                } else {
                    Text(
                        "Enter the email you signed up with. We will send you a link to choose a new password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Start,
                    )
                    TMTextField(
                        value = state.email,
                        onValueChange = { onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                        label = "Email",
                        leadingIcon = Icons.Rounded.Email,
                        error = state.error,
                        enabled = !state.loading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onEvent(ForgotPasswordEvent.Submit) }),
                    )
                    TMPrimaryButton(
                        text = "Send link",
                        onClick = { onEvent(ForgotPasswordEvent.Submit) },
                        loading = state.loading,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun ForgotPasswordEmptyPreview() = TravelMateTheme {
    ForgotPasswordContent(ForgotPasswordUiState(), {}, {})
}

@ThemePreviews
@Composable
private fun ForgotPasswordErrorPreview() = TravelMateTheme {
    ForgotPasswordContent(ForgotPasswordUiState(email = "sample@", error = "Enter a valid email"), {}, {})
}

@ThemePreviews
@Composable
private fun ForgotPasswordSentPreview() = TravelMateTheme {
    ForgotPasswordContent(ForgotPasswordUiState(email = "sample@example.com", sent = true), {}, {})
}
