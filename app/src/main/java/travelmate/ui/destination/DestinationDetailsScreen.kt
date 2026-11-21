package com.travelmate.ui.screens.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Destination
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMSecondaryButton
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Route:destination/{destinationId}. Gallery, rating, stats, About, filterable Highlights and Tips,
 * with Save and Plan a trip pinned at the bottom (as in the Milestone 2 design).
 */
@Composable
fun DestinationDetailsScreen(
    onBack: () -> Unit,
    onPlanTrip: (String) -> Unit,
    viewModel: DestinationDetailsViewModel = viewModel(factory = DestinationDetailsViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AmbientBackground {
        val destination = state.destination
        when {
            state.loading -> CircularProgressIndicator(
                color = TravelMateTheme.colors.primary,
                modifier = Modifier.align(Alignment.Center),
            )
            state.error != null -> MessageWithBack(onBack) {
                EmptyState(icon = Icons.Rounded.CloudOff, title = "Could not load this destination", message = state.error)
            }
            state.notFound || destination == null -> MessageWithBack(onBack) {
                EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = "Not found",
                    message = "This destination is no longer available.",
                    actionLabel = "Back to Explore",
                    onAction = onBack,
                )
            }
            else -> DestinationDetailsContent(
                state = state,
                destination = destination,
                onEvent = viewModel::onEvent,
                onBack = onBack,
                onPlanTrip = { onPlanTrip(destination.id) },
            )
        }
    }

    state.selectedPlace?.let { place ->
        PlaceSheet(
            place = place,
            isFavorite = place.id in state.favoritePlaceIds,
            onToggleFavorite = { viewModel.onEvent(DestinationDetailsEvent.TogglePlaceFavorite(place)) },
            onDismiss = { viewModel.onEvent(DestinationDetailsEvent.PlaceSelected(null)) },
        )
    }
}

@Composable
fun DestinationDetailsContent(
    state: DestinationDetailsUiState,
    destination: Destination,
    onEvent: (DestinationDetailsEvent) -> Unit,
    onBack: () -> Unit,
    onPlanTrip: () -> Unit,
) {
    val colors = TravelMateTheme.colors
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.section),
        ) {
            DestinationHero(
                destination = destination,
                onBack = onBack,
                isFavorite = state.isFavorite,
                onToggleFavorite = { onEvent(DestinationDetailsEvent.ToggleFavorite) },
            )
            DestinationHeader(destination = destination, modifier = Modifier.padding(horizontal = Spacing.gutter))
            StatRow(destination = destination, modifier = Modifier.padding(horizontal = Spacing.gutter))
            AboutSection(
                destination = destination,
                expanded = state.aboutExpanded,
                onToggle = { onEvent(DestinationDetailsEvent.ToggleAbout) },
                modifier = Modifier.padding(horizontal = Spacing.gutter),
            )
            HighlightsSection(
                places = state.filteredPlaces,
                filter = state.filter,
                onFilterChange = { onEvent(DestinationDetailsEvent.FilterChanged(it)) },
                onPlaceClick = { onEvent(DestinationDetailsEvent.PlaceSelected(it)) },
            )
            if (destination.tips.isNotEmpty()) {
                TipsSection(
                    tips = destination.tips,
                    expanded = state.tipsExpanded,
                    onToggle = { onEvent(DestinationDetailsEvent.ToggleTips) },
                    modifier = Modifier.padding(horizontal = Spacing.gutter),
                )
            }
            // Room for the pinned buttons.
            Spacer(Modifier.navigationBarsPadding().height(56.dp + Spacing.xxl))
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, colors.bgBottom)))
                .navigationBarsPadding()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TMSecondaryButton(
                text = if (state.isFavorite) "Saved" else "Save",
                onClick = { onEvent(DestinationDetailsEvent.ToggleFavorite) },
                icon = if (state.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            )
            TMPrimaryButton(text = "Plan a trip", onClick = onPlanTrip, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DestinationHeader(destination: Destination, modifier: Modifier = Modifier) {
    val colors = TravelMateTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(destination.name, style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Icon(Icons.Rounded.Place, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
            Text(destination.country, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.padding(start = Spacing.md).size(16.dp),
            )
            Text(Formatters.rating(destination.rating), style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
            if (destination.reviewCount > 0) {
                Text(
                    "(${Formatters.compactCount(destination.reviewCount)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun MessageWithBack(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TMTopBar(title = "", onBack = onBack)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
    }
}
