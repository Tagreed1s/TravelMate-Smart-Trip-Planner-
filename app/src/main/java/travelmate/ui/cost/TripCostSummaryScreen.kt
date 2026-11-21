package com.travelmate.ui.screens.cost



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.core.Formatters
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.RemoteImage
import com.travelmate.ui.components.TMSecondaryButton
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.components.icon
import com.travelmate.ui.components.label
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.NumberStyle
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Route: trip_cost_summary/{tripId}. Flights, accommodation, food, transportation and activities
 * (calculated plus the user's own expenses), what is left of the budget, the estimated total,
 * and "Edit expenses" (as in the Milestone 2 design).
 */
@Composable
fun TripCostSummaryScreen(
    onBack: () -> Unit,
    viewModel: TripCostSummaryViewModel = viewModel(factory = TripCostSummaryViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(TripCostSummaryEvent.MessageShown)
        }
    }

    TripCostSummaryContent(state = state, onEvent = viewModel::onEvent, onBack = onBack, snackbarHostState = snackbarHostState)

    val trip = state.trip
    if (state.showEditExpenses && trip != null) {
        EditExpensesSheet(
            expenses = trip.expenses,
            form = state.form,
            saving = state.saving,
            onEvent = viewModel::onEvent,
            onDismiss = { viewModel.onEvent(TripCostSummaryEvent.EditExpensesDismissed) },
        )
    }
}

@Composable
fun TripCostSummaryContent(
    state: TripCostSummaryUiState,
    onEvent: (TripCostSummaryEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val trip = state.trip
    val cost = state.cost
    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = { TMTopBar(title = "Trip cost summary", onBack = onBack) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when {
                    state.loading -> CircularProgressIndicator(
                        color = TravelMateTheme.colors.primary,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.error != null -> EmptyState(
                        icon = Icons.Rounded.CloudOff,
                        title = "Could not load the costs",
                        message = state.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.notFound || trip == null || cost == null -> EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = "Trip not found",
                        actionLabel = "Go back",
                        onAction = onBack,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> CostSummaryBody(state = state, trip = trip, cost = cost, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun CostSummaryBody(
    state: TripCostSummaryUiState,
    trip: Trip,
    cost: TripCost,
    onEvent: (TripCostSummaryEvent) -> Unit,
) {
    val colors = TravelMateTheme.colors
    val remaining = cost.remainingSar(trip.budgetSar)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        TripHeaderCard(trip)

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text("Cost breakdown", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                state.lines.forEach { line -> CostRow(line) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Savings, contentDescription = null, tint = if (remaining >= 0) colors.success else colors.error)
                    Text(
                        if (remaining >= 0) "Estimated savings" else "Over budget by",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (remaining >= 0) colors.success else colors.error,
                        modifier = Modifier.weight(1f).padding(start = Spacing.md),
                    )
                    Text(
                        Formatters.money(if (remaining >= 0) remaining else -remaining),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (remaining >= 0) colors.success else colors.error,
                    )
                }
            }
        }

        TotalCard(totalSar = cost.totalSar, budgetSar = trip.budgetSar, travelers = trip.travelers)

        TMSecondaryButton(
            text = "Edit expenses",
            onClick = { onEvent(TripCostSummaryEvent.EditExpensesClicked) },
            icon = Icons.Rounded.Edit,
            modifier = Modifier.fillMaxWidth(),
        )

        state.stayNote?.let {
            Text("Accommodation: $it", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        }
    }
}

/** Destination photo, trip name, dates and travelers. */
@Composable
private fun TripHeaderCard(trip: Trip) {
    val colors = TravelMateTheme.colors
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            RemoteImage(url = trip.heroImageUrl, contentDescription = null, modifier = Modifier.size(56.dp).clip(TMShapes.chip))
            Column {
                Text(trip.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(
                    "${Formatters.dateRange(trip.startDate, trip.endDate)} · ${trip.travelers} traveler${if (trip.travelers == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

/** Icon, label, "incl. SAR x added" when the user added expenses, and the amount. */
@Composable
private fun CostRow(line: CostLine) {
    val colors = TravelMateTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Icon(line.category.icon, contentDescription = null, tint = colors.textSecondary)
        Column(Modifier.weight(1f)) {
            Text(line.category.label, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            if (line.addedSar > 0 && line.calculatedSar > 0) {
                Text(
                    "incl. ${Formatters.money(line.addedSar)} you added",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                )
            } else if (line.category == ExpenseCategory.FLIGHTS && line.addedSar == 0) {
                Text("Add flights in Edit expenses", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            }
        }
        Text(Formatters.money(line.totalSar), style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
    }
}

/** Dark gradient card with the estimated total (Milestone 2 design). */
@Composable
private fun TotalCard(totalSar: Int, budgetSar: Int, travelers: Int) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TMShapes.card)
            .background(Brush.horizontalGradient(listOf(colors.primary, colors.primaryEnd)))
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Estimated total", style = MaterialTheme.typography.titleMedium, color = colors.onPrimary)
            Text(
                "for $travelers traveler${if (travelers == 1) "" else "s"} · budget ${Formatters.money(budgetSar)}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onPrimary.copy(alpha = 0.85f),
            )
        }
        Text(Formatters.money(totalSar), style = NumberStyle.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize), color = colors.onPrimary)
    }
}

@ThemePreviews
@Composable
private fun TripCostSummaryLoadingPreview() = TravelMateTheme {
    TripCostSummaryContent(TripCostSummaryUiState(loading = true), {}, {}, remember { SnackbarHostState() })
}

@ThemePreviews
@Composable
private fun TripCostSummaryErrorPreview() = TravelMateTheme {
    TripCostSummaryContent(TripCostSummaryUiState(loading = false, error = "SAMPLE error"), {}, {}, remember { SnackbarHostState() })
}

@ThemePreviews
@Composable
private fun TotalCardPreview() = PreviewSurface {
    TotalCard(totalSar = 3_840, budgetSar = 5_400, travelers = 2)
}
