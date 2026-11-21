package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Empty or error state used by every screen: 64 dp glowing icon circle, a title, one line,
 * and an optional action button (for example "Try again" or "Explore destinations").
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = TravelMateTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(colors.accentGlow, CircleShape)
                .border(1.dp, colors.primary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary)
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (message != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.xl))
            TMPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

@ThemePreviews
@Composable
private fun EmptyStatePreview() = PreviewSurface {
    EmptyState(
        icon = Icons.Rounded.Luggage,
        title = "No trips yet",
        message = "Plan your first trip from Explore.",
        actionLabel = "Explore destinations",
        onAction = {},
    )
}
