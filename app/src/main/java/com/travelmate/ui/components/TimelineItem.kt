package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.domain.model.PlaceType
import com.travelmate.ui.theme.NumberStyle
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.TravelMateTheme

/**
 * One itinerary entry: start over end time on the left, a dot and line in the middle,
 * and a glass card with thumbnail, name and "Type · SAR x per person".
 */
@Composable
fun TimelineItem(
    startMin: Int,
    endMin: Int,
    name: String,
    type: PlaceType,
    priceSar: Int,
    imageUrl: String,
    modifier: Modifier = Modifier,
    showLine: Boolean = true,
    /** When set, a delete icon is shown on the card (as in the Milestone 2 design). */
    onRemove: (() -> Unit)? = null,
) {
    val colors = TravelMateTheme.colors
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(Modifier.width(56.dp).padding(top = Spacing.md)) {
            Text(Formatters.time(startMin), style = NumberStyle, color = colors.textPrimary)
            Text(Formatters.time(endMin), style = NumberStyle, color = colors.textSecondary)
        }
        Box(Modifier.width(20.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            if (showLine) {
                Box(
                    Modifier
                        .padding(top = Spacing.lg)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.primary.copy(alpha = 0.3f)),
                )
            }
            Box(
                Modifier
                    .padding(top = Spacing.lg)
                    .size(10.dp)
                    .background(colors.primary, CircleShape),
            )
        }
        GlassCard(
            modifier = Modifier.weight(1f).padding(bottom = Spacing.md),
            contentPadding = PaddingValues(Spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                RemoteImage(url = imageUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(TMShapes.chip))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${type.label} · " +
                            if (priceSar == 0) "Free" else "${Formatters.money(priceSar)} per person",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                }
                if (onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove $name", tint = colors.textSecondary)
                    }
                }
            }
        }
    }
}
