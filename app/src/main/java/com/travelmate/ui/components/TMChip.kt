package com.travelmate.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/** 36 dp filter chip; selected = accent glow + check icon. The touch target stays 48 dp. */
@Composable
fun TMChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(36.dp)
            .clip(TMShapes.chip)
            .background(if (selected) colors.accentGlow else colors.surfaceGlass)
            .border(1.dp, if (selected) colors.primary else colors.outlineGlass, TMShapes.chip)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        AnimatedVisibility(visible = selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.primary else colors.textPrimary,
        )
    }
}

@ThemePreviews
@Composable
private fun TMChipPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        TMChip(label = "Attractions", selected = true, onClick = {})
        TMChip(label = "Events", selected = false, onClick = {})
    }
}
