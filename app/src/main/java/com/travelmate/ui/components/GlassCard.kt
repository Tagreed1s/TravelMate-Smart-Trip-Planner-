package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** Glass surface: surfaceGlass fill, 1 dp outlineGlass border, 20 dp corners, 16 dp padding. No shadow. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TravelMateTheme.colors
    Column(
        modifier = modifier
            .clip(TMShapes.card)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.outlineGlass, TMShapes.card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

@ThemePreviews
@Composable
private fun GlassCardPreview() = PreviewSurface {
    GlassCard {
        Text("Best time", style = MaterialTheme.typography.labelSmall, color = TravelMateTheme.colors.textSecondary)
        Text("Nov - Mar", style = MaterialTheme.typography.titleMedium, color = TravelMateTheme.colors.textPrimary)
    }
}
