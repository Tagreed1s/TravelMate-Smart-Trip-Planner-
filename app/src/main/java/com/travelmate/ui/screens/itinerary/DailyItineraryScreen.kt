package com.travelmate.ui.screens.itinerary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Trip
import com.travelmate.ui.components.BudgetBar
import com.travelmate.ui.components.DayPills
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMSecondaryButton
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.addactivity.AddActivitySheet
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.NumberStyle
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Route: daily_itinerary/{tripId}. Day tabs, the day's timeline (remove with the delete icon or a swipe),
 * Add activity, the budget bar, and the View map and Cost summary buttons from the Milestone 2 design.
 */
@Composable
fun DailyItineraryScreen(
    onBack: () -> Unit,
    onOpenMap: (String) -> Unit,
    onOpenCostSummary: (String) -> Unit,
    viewModel: DailyItineraryViewModel = viewModel(factory = DailyItineraryViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBack by rememberUpdatedState(onBack)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DailyItineraryEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
                DailyItineraryEffect.TripDeleted -> currentOnBack()
            }
        }
    }

    AmbientBackground {
        val trip = state.trip
        when {
            state.loading || state.deleting -> CircularProgressIndicator(
                color = TravelMateTheme.colors.primary,
                modifier = Modifier.align(Alignment.Center),
            )
            state.error != null -> MessageWithBack(onBack) {
                EmptyState(icon = Icons.Rounded.CloudOff, title = "Could not load this trip", message = state.error)
            }
            state.notFound || trip == null -> MessageWithBack(onBack) {
                EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = "Trip not found",
                    message = "This trip no longer exists.",
                    actionLabel = "Go back",
                    onAction = onBack,
                )
            }
            else -> DailyItineraryContent(
                state = state,
                trip = trip,
                onEvent = viewModel::onEvent,
                onBack = onBack,
                onOpenMap = { onOpenMap(trip.id) },
                onOpenCostSummary = { onOpenCostSummary(trip.id) },
                snackbarHostState = snackbarHostState,
            )
        }
    }

    val trip = state.trip
    if (state.showAddActivity && trip != null) {
        AddActivitySheet(
            tripId = trip.id,
            day = state.selectedDay,
            onDismiss = { viewModel.onEvent(DailyItineraryEvent.AddActivityDismissed) },
        )
    }
    if (state.showRenameDialog && trip != null) {
        RenameTripDialog(
            initialName = trip.name,
            onDismiss = { viewModel.onEvent(DailyItineraryEvent.RenameDismissed) },
            onConfirm = { viewModel.onEvent(DailyItineraryEvent.RenameConfirmed(it)) },
        )
    }
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(DailyItineraryEvent.DeleteDismissed) },
            containerColor = TravelMateTheme.colors.surface,
            title = { Text("Delete this trip?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("The plan and its expenses will be removed from all your devices.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(DailyItineraryEvent.DeleteConfirmed) }) {
                    Text("Delete", color = TravelMateTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(DailyItineraryEvent.DeleteDismissed) }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun DailyItineraryContent(
    state: DailyItineraryUiState,
    trip: Trip,
    onEvent: (DailyItineraryEvent) -> Unit,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenCostSummary: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TMTopBar(
                title = trip.name,
                subtitle = "${Formatters.dateRange(trip.startDate, trip.endDate)} · " +
                        "${trip.travelers} traveler${if (trip.travelers == 1) "" else "s"}",
                onBack = onBack,
                actions = {
                    TripMenu(
                        onRename = { onEvent(DailyItineraryEvent.RenameClicked) },
                        onDelete = { onEvent(DailyItineraryEvent.DeleteClicked) },
                    )
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                BudgetBar(
                    totalSar = state.cost?.totalSar ?: trip.totalCostSar,
                    budgetSar = trip.budgetSar,
                    onClick = onOpenCostSummary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    TMSecondaryButton(
                        text = "View map",
                        onClick = onOpenMap,
                        icon = Icons.Rounded.Map,
                        modifier = Modifier.weight(1f),
                    )
                    TMPrimaryButton(text = "Cost summary", onClick = onOpenCostSummary, modifier = Modifier.weight(1f))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            DayPills(
                days = trip.days,
                startDate = trip.startDate,
                selectedDay = state.selectedDay,
                onSelect = { onEvent(DailyItineraryEvent.DaySelected(it)) },
            )
            AnimatedContent(
                targetState = state.selectedDay,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "day",
                modifier = Modifier.weight(1f),
            ) { day ->
                Column(Modifier.fillMaxSize()) {
                    DayHeader(trip = trip, day = day, dayCostSar = state.dayCosts[day])
                    val items = trip.itemsForDay(day)
                    if (items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Rounded.EventBusy,
                                title = "Nothing planned for this day",
                                message = "Add a place from this destination.",
                                actionLabel = "Add activity",
                                onAction = { onEvent(DailyItineraryEvent.AddActivityClicked) },
                            )
                        }
                    } else {
                        DayTimeline(
                            items = items,
                            placesById = state.placesById,
                            onRemove = { onEvent(DailyItineraryEvent.RemoveItem(it)) },
                            contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.sm),
                            footer = {
                                TMSecondaryButton(
                                    text = "Add activity",
                                    onClick = { onEvent(DailyItineraryEvent.AddActivityClicked) },
                                    icon = Icons.Rounded.Add,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(trip: Trip, day: Int, dayCostSar: Int?) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Day $day · ${Formatters.weekdayDayMonth(trip.startDate.plusDays((day - 1).toLong()))}",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (dayCostSar != null) {
            Text(Formatters.money(dayCostSar), style = NumberStyle, color = colors.textSecondary)
        }
    }
}

@Composable
private fun TripMenu(onRename: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Trip options", tint = TravelMateTheme.colors.textPrimary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Rename trip") }, onClick = { expanded = false; onRename() })
            DropdownMenuItem(
                text = { Text("Delete trip", color = TravelMateTheme.colors.error) },
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}

@Composable
private fun RenameTripDialog(initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    val canSave = name.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TravelMateTheme.colors.surface,
        title = { Text("Rename trip", style = MaterialTheme.typography.headlineSmall) },
        text = {
            TMTextField(
                value = name,
                onValueChange = { name = it.take(MAX_TRIP_NAME) },
                label = "Trip name",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSave) onConfirm(name) }),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = canSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MessageWithBack(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TMTopBar(title = "", onBack = onBack)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
    }
}

private const val MAX_TRIP_NAME = 60
