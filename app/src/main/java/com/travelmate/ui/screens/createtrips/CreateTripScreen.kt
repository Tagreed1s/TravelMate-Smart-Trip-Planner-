package com.travelmate.ui.screens.createtrips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.core.Formatters
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.usecase.PlanningRules
import com.travelmate.domain.usecase.TripField
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.TMChip
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMStepper
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.components.pluralLabel
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Route: create_trip/{destinationId}. Every field starts with a sensible value (start in a week, 3 days,
 * 2 travelers, suggested budget), so "Create plan" works with one tap. The plan is saved automatically.
 */
@Composable
fun CreateTripScreen(
    onBack: () -> Unit,
    onTripCreated: (String) -> Unit,
    viewModel: CreateTripViewModel = viewModel(factory = CreateTripViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnTripCreated by rememberUpdatedState(onTripCreated)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CreateTripEffect.TripCreated -> currentOnTripCreated(effect.tripId)
                is CreateTripEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    CreateTripContent(state = state, onEvent = viewModel::onEvent, onBack = onBack, snackbarHostState = snackbarHostState)
}

@Composable
fun CreateTripContent(
    state: CreateTripUiState,
    onEvent: (CreateTripEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val focusManager = LocalFocusManager.current
    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                TMTopBar(
                    title = "Create trip",
                    subtitle = "Tell us about your trip and we'll build your plan.",
                    onBack = onBack,
                )
            },
            bottomBar = {
                Box(
                    Modifier
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = Spacing.gutter, vertical = Spacing.md),
                ) {
                    TMPrimaryButton(
                        text = "Create plan",
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(CreateTripEvent.Generate)
                        },
                        loading = state.generating || !state.placesLoaded,
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            if (state.destination == null && state.destinations.isEmpty()) {
                // Loading state: the catalog is not read yet.
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TravelMateTheme.colors.primary)
                }
            } else {
                CreateTripForm(state = state, onEvent = onEvent, modifier = Modifier.padding(padding))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateTripForm(
    state: CreateTripUiState,
    onEvent: (CreateTripEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TravelMateTheme.colors
    val focusManager = LocalFocusManager.current
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.gutter)
            .padding(top = Spacing.sm, bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        DestinationField(state = state, onEvent = onEvent)

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            GlassCard(modifier = Modifier.weight(1f), onClick = { showDatePicker = true }) {
                FieldLabel("Start")
                Text(Formatters.shortDate(state.startDate), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                FieldError(state.errors[TripField.START_DATE])
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                FieldLabel("Number of days")
                TMStepper(
                    value = state.days,
                    onValueChange = { onEvent(CreateTripEvent.DaysChanged(it)) },
                    range = PlanningRules.MIN_DAYS..PlanningRules.MAX_DAYS,
                    valueLabel = "days",
                )
                FieldError(state.errors[TripField.DAYS])
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    FieldLabel("Travelers")
                    Text(
                        "${(state.travelers + 1) / 2} room${if (state.travelers > 2) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                }
                TMStepper(
                    value = state.travelers,
                    onValueChange = { onEvent(CreateTripEvent.TravelersChanged(it)) },
                    range = PlanningRules.MIN_TRAVELERS..PlanningRules.MAX_TRAVELERS,
                    valueLabel = "travelers",
                )
            }
            FieldError(state.errors[TripField.TRAVELERS])
        }

        Column {
            TMTextField(
                value = state.budgetText,
                onValueChange = { onEvent(CreateTripEvent.BudgetChanged(it)) },
                label = "Budget (SAR)",
                leadingIcon = Icons.Rounded.Payments,
                error = state.errors[TripField.BUDGET],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
            BudgetHints(state = state, onUse = { onEvent(CreateTripEvent.UseSuggestion) })
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("What do you like?", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PlaceType.entries.forEach { type ->
                    TMChip(
                        label = type.pluralLabel,
                        selected = type in state.interests,
                        onClick = { onEvent(CreateTripEvent.InterestToggled(type)) },
                    )
                }
            }
            FieldError(state.errors[TripField.INTERESTS])
        }
    }

    if (showDatePicker) {
        StartDatePickerDialog(
            initialDate = state.startDate,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                showDatePicker = false
                onEvent(CreateTripEvent.StartDateChanged(it))
            },
        )
    }
}

/** Destination, with a "Change" menu listing every destination. */
@Composable
private fun DestinationField(state: CreateTripUiState, onEvent: (CreateTripEvent) -> Unit) {
    val colors = TravelMateTheme.colors
    var expanded by remember { mutableStateOf(false) }
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                FieldLabel("Destination")
                Text(state.destination?.name ?: "Choose a destination", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                state.destination?.country?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                }
                FieldError(state.errors[TripField.DESTINATION])
            }
            Box {
                TextButton(onClick = { expanded = true }, enabled = state.destinations.isNotEmpty()) {
                    Text("Change", color = colors.secondary)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.destinations.forEach { destination ->
                        DropdownMenuItem(
                            text = { Text(destination.name) },
                            onClick = {
                                expanded = false
                                onEvent(CreateTripEvent.DestinationChanged(destination.id))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetHints(state: CreateTripUiState, onUse: () -> Unit) {
    val colors = TravelMateTheme.colors
    val suggested = state.suggestedBudgetSar ?: return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Suggested: ${Formatters.money(suggested)}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (state.budgetText != suggested.toString()) {
            TextButton(onClick = onUse) { Text("Use", color = colors.secondary) }
        }
    }
    state.fixedCostSar?.let {
        Text(
            "Stay and transport: ${Formatters.money(it)}",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
        )
    }
    Spacer(Modifier.height(Spacing.xs))
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TravelMateTheme.colors.textSecondary)
}

@Composable
private fun FieldError(text: String?) {
    if (text == null) return
    Text(text, style = MaterialTheme.typography.labelSmall, color = TravelMateTheme.colors.error)
}
