package com.travelmate.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.domain.model.User
import com.travelmate.ui.components.Avatar
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.NumberStyle
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Route: profile (bottom bar tab). Name, email, trip and saved counts, and log out with a confirmation. */
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileContent(state = state, contentPadding = contentPadding, onEvent = viewModel::onEvent)

    if (state.showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ProfileEvent.LogoutDismissed) },
            containerColor = TravelMateTheme.colors.surface,
            title = { Text("Log out of TravelMate?", style = MaterialTheme.typography.headlineSmall) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(ProfileEvent.LogoutConfirmed) }) {
                    Text("Log out", color = TravelMateTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ProfileEvent.LogoutDismissed) }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun ProfileContent(state: ProfileUiState, contentPadding: PaddingValues, onEvent: (ProfileEvent) -> Unit) {
    val colors = TravelMateTheme.colors
    AmbientBackground {
        Column(Modifier.fillMaxSize()) {
            TMTopBar(title = "Profile")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.gutter)
                    .padding(bottom = contentPadding.calculateBottomPadding() + Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Spacer(Modifier.height(Spacing.md))
                Avatar(initials = state.user?.initials ?: "?", size = 72.dp)
                Spacer(Modifier.height(Spacing.xs))
                Text(state.user?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                Text(state.user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(label = "Trips", value = state.tripsCount, modifier = Modifier.weight(1f))
                    StatTile(label = "Saved", value = state.savedCount, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(Spacing.lg))
                OutlinedButton(
                    onClick = { onEvent(ProfileEvent.LogoutClicked) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = TMShapes.pill,
                    border = BorderStroke(1.dp, colors.error.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Log out", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** Count in the display font over its label; "–" while loading. */
@Composable
private fun StatTile(label: String, value: Int?, modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    GlassCard(modifier) {
        Text(value?.toString() ?: "–", style = NumberStyle.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize), color = colors.textPrimary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
    }
}

@ThemePreviews
@Composable
private fun ProfilePreview() = TravelMateTheme {
    ProfileContent(
        state = ProfileUiState(user = User("sample", "SAMPLE User", "sample@example.com"), tripsCount = 3, savedCount = 5),
        contentPadding = PaddingValues(),
        onEvent = {},
    )
}

@ThemePreviews
@Composable
private fun ProfileLoadingPreview() = TravelMateTheme {
    ProfileContent(state = ProfileUiState(user = User("sample", "SAMPLE User", "sample@example.com")), contentPadding = PaddingValues(), onEvent = {})
}
