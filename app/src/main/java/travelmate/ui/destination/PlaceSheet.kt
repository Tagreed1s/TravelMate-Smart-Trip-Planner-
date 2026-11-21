package com.travelmate.ui.screens.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Place
import com.travelmate.ui.components.FavoriteSlot
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.RemoteImage
import com.travelmate.ui.components.icon
import com.travelmate.ui.components.label
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.TravelMateTheme

/**Place details opened from Highlights: photo, description, hours, visit length, price, and a heart to save it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSheet(
    place: Place,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TravelMateTheme.colors.surface,
        shape = TMShapes.sheet,
    ) {
        PlaceDetails(place, isFavorite, onToggleFavorite)
    }
}

@Composable
fun PlaceDetails(place: Place, isFavorite: Boolean? = null, onToggleFavorite: (() -> Unit)? = null) {
    val colors = TravelMateTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.gutter)
            .padding(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        RemoteImage(
            url = place.imageUrl,
            contentDescription = place.name,
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(TMShapes.card),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Icon(place.type.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(place.type.label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.padding(start = Spacing.sm).size(16.dp),
                    )
                    Text(Formatters.rating(place.rating), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                }
            }
            FavoriteSlot(isFavorite = isFavorite, onToggleFavorite = onToggleFavorite)
        }
        if (place.description.isNotBlank()) {
            Text(place.description, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary)
        }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                if (place.openingHours.isNotBlank()) InfoRow(Icons.Rounded.Schedule, "Opening hours", place.openingHours)
                place.schedule?.let { InfoRow(Icons.Rounded.Event, "Schedule", it) }
                InfoRow(Icons.Rounded.Timer, "Typical visit", Formatters.duration(place.durationMin))
                InfoRow(Icons.Rounded.Payments, "Price per person", Formatters.priceOrFree(place.priceSar))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    val colors = TravelMateTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(22.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
        }
    }
}
