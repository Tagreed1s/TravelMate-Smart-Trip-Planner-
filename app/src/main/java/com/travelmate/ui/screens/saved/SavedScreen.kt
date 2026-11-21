package com.travelmate.ui.screens.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Trip
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.RemoteImage
import com.travelmate.ui.components.TMSegmented
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.components.label
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Route: saved (bottom bar tab). Saved Trips and Favorites merged into one screen with two tabs,
 * as asked in the instructor's change request: Trips | Destinations (saved destinations and places).
 */
@Composable
fun SavedScreen(
    contentPadding: PaddingValues,
    onOpenTrip: (String) -> Unit,
    onOpenDestination: (String) -> Unit,
    onExplore: () -> Unit,
    viewModel: SavedViewModel = viewModel(factory = SavedViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SavedEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    SavedContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onOpenTrip = onOpenTrip,
        onOpenDestination = onOpenDestination,
        onExplore = onExplore,
        snackbarHostState = snackbarHostState,
    )

    state.tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SavedEvent.DeleteTripDismissed) },
            containerColor = TravelMateTheme.colors.surface,
            title = { Text("Delete ${trip.name}?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("The plan and its expenses will be removed from all your devices.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(SavedEvent.DeleteTripConfirmed) }) {
                    Text("Delete", color = TravelMateTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(SavedEvent.DeleteTripDismissed) }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun SavedContent(
    state: SavedUiState,
    contentPadding: PaddingValues,
    onEvent: (SavedEvent) -> Unit,
    onOpenTrip: (String) -> Unit,
    onOpenDestination: (String) -> Unit,
    onExplore: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbarHostState, Modifier.padding(contentPadding)) },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                TMTopBar(title = "Saved")
                TMSegmented(
                    options = SavedTab.entries.map { it.label },
                    selectedIndex = state.tab.ordinal,
                    onSelect = { onEvent(SavedEvent.TabSelected(SavedTab.entries[it])) },
                    modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                )
                val listPadding = PaddingValues(
                    start = Spacing.gutter,
                    end = Spacing.gutter,
                    top = Spacing.sm,
                    bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (state.tab) {
                        SavedTab.TRIPS -> TripsTab(state, listPadding, onEvent, onOpenTrip, onExplore)
                        SavedTab.DESTINATIONS -> DestinationsTab(state, listPadding, onEvent, onOpenDestination, onExplore)
                    }
                }
            }
        }
    }
}

@Composable
private fun TripsTab(
    state: SavedUiState,
    listPadding: PaddingValues,
    onEvent: (SavedEvent) -> Unit,
    onOpenTrip: (String) -> Unit,
    onExplore: () -> Unit,
) {
    when {
        state.tripsLoading -> Loading()
        state.tripsError != null -> Centered {
            EmptyState(icon = Icons.Rounded.CloudOff, title = "Could not load your trips", message = state.tripsError)
        }
        state.trips.isEmpty() -> Centered {
            EmptyState(
                icon = Icons.Rounded.Luggage,
                title = "No trips yet",
                message = "Pick a destination and create your first plan.",
                actionLabel = "Explore destinations",
                onAction = onExplore,
            )
        }
        else -> LazyColumn(contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            items(state.trips, key = { it.id }) { trip ->
                TripRow(
                    trip = trip,
                    onClick = { onOpenTrip(trip.id) },
                    onDelete = { onEvent(SavedEvent.DeleteTripClicked(trip)) },
                )
            }
        }
    }
}

@Composable
private fun DestinationsTab(
    state: SavedUiState,
    listPadding: PaddingValues,
    onEvent: (SavedEvent) -> Unit,
    onOpenDestination: (String) -> Unit,
    onExplore: () -> Unit,
) {
    when {
        state.favoritesLoading -> Loading()
        state.favoritesError != null -> Centered {
            EmptyState(icon = Icons.Rounded.CloudOff, title = "Could not load your saved items", message = state.favoritesError)
        }
        state.destinations.isEmpty() && state.places.isEmpty() -> Centered {
            EmptyState(
                icon = Icons.Rounded.FavoriteBorder,
                title = "Nothing saved yet",
                message = "Tap the heart on a destination or a place to keep it here.",
                actionLabel = "Explore destinations",
                onAction = onExplore,
            )
        }
        else -> LazyColumn(contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            if (state.destinations.isNotEmpty()) {
                item(key = "destinations-title") { SectionTitle("Destinations") }
                items(state.destinations, key = { "d-" + it.id }) { destination ->
                    SavedRow(
                        imageUrl = destination.heroImageUrl,
                        title = destination.name,
                        subtitle = savedDestinationLine(destination),
                        onClick = { onOpenDestination(destination.id) },
                        onRemove = { onEvent(SavedEvent.RemoveDestination(destination.id)) },
                    )
                }
            }
            if (state.places.isNotEmpty()) {
                item(key = "places-title") { SectionTitle("Places") }
                items(state.places, key = { "p-" + it.place.id }) { saved ->
                    SavedRow(
                        imageUrl = saved.place.imageUrl,
                        title = saved.place.name,
                        subtitle = "${saved.place.type.label} · ${saved.destinationName}",
                        onClick = { onOpenDestination(saved.place.destinationId) },
                        onRemove = { onEvent(SavedEvent.RemovePlace(saved.place.id, saved.place.destinationId)) },
                    )
                }
            }
        }
    }
}

/** Destination image, trip name, "23 - 25 Oct · 3 days" and "SAR 3,840 · 8 places", with a delete button. */
@Composable
private fun TripRow(trip: Trip, onClick: () -> Unit, onDelete: () -> Unit) {
    val colors = TravelMateTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            RemoteImage(url = trip.heroImageUrl, contentDescription = null, modifier = Modifier.size(72.dp).clip(TMShapes.field))
            Column(Modifier.weight(1f)) {
                Text(trip.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${Formatters.dateRange(trip.startDate, trip.endDate)} · ${trip.days} day${if (trip.days == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
                Text(
                    "${Formatters.money(trip.totalCostSar)} · ${trip.placesCount} place${if (trip.placesCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete ${trip.name}", tint = colors.textSecondary)
            }
        }
    }
}

/** Saved destination or place: image, name, one line of detail, and a filled heart that removes it. */
@Composable
private fun SavedRow(imageUrl: String, title: String, subtitle: String, onClick: () -> Unit, onRemove: () -> Unit) {
    val colors = TravelMateTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            RemoteImage(url = imageUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(TMShapes.field))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Favorite, contentDescription = "Remove $title from saved", tint = colors.primary)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = TravelMateTheme.colors.textPrimary)
}

@Composable
private fun Loading() {
    Centered { CircularProgressIndicator(color = TravelMateTheme.colors.primary) }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

private fun savedDestinationLine(destination: Destination): String =
    "${destination.country} · ★ ${Formatters.rating(destination.rating)}"

@ThemePreviews
@Composable
private fun SavedTripsEmptyPreview() = TravelMateTheme {
    SavedContent(
        state = SavedUiState(tripsLoading = false, favoritesLoading = false),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenTrip = {},
        onOpenDestination = {},
        onExplore = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}

@ThemePreviews
@Composable
private fun SavedDestinationsEmptyPreview() = TravelMateTheme {
    SavedContent(
        state = SavedUiState(tab = SavedTab.DESTINATIONS, tripsLoading = false, favoritesLoading = false),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenTrip = {},
        onOpenDestination = {},
        onExplore = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}

@ThemePreviews
@Composable
private fun SavedErrorPreview() = TravelMateTheme {
    SavedContent(
        state = SavedUiState(tripsLoading = false, tripsError = "SAMPLE error"),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenTrip = {},
        onOpenDestination = {},
        onExplore = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}
