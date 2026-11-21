package com.travelmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Destination
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Full-width 4:3 destination card: image with a bottom scrim, name, country and rating,
 * a small "Plan" pill, and a heart (top-right) that saves the destination.
 */
@Composable
fun DestinationCard(
    destination: Destination,
    onClick: () -> Unit,
    onPlan: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
) {
    val colors = TravelMateTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(TMShapes.card)
            .border(1.dp, colors.outlineGlass, TMShapes.card)
            .clickable(onClickLabel = "Open ${destination.name}", onClick = onClick),
    ) {
        RemoteImage(url = destination.heroImageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
        ImageScrim(Modifier.fillMaxSize())
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                MetaRow(country = destination.country, rating = destination.rating)
            }
            SmallPillButton(text = "Plan", onClick = onPlan)
        }
        FavoriteSlot(
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.sm),
        )
    }
}

@Composable
private fun MetaRow(country: String, rating: Double) {
    val colors = TravelMateTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(Icons.Rounded.Place, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
        Text(country, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Icon(
            Icons.Rounded.Star,
            contentDescription = null,
            tint = colors.warning,
            modifier = Modifier.padding(start = Spacing.sm).size(14.dp),
        )
        Text(Formatters.rating(rating), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
    }
}

/** Bottom scrim that fades an image into the background color so text stays readable. */
@Composable
fun ImageScrim(modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                0.35f to Color.Transparent,
                1f to colors.bgBottom.copy(alpha = 0.95f),
            ),
        ),
    )
}

/** Small gradient pill (36 dp high, 48 dp touch target). */
@Composable
fun SmallPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(36.dp)
            .clip(TMShapes.pill)
            .background(Brush.horizontalGradient(listOf(colors.primary, colors.primaryEnd)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.onPrimary)
    }
}
