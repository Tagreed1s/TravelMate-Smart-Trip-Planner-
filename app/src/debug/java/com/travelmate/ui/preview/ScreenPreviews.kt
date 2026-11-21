package com.travelmate.ui.preview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.domain.model.User
import com.travelmate.domain.usecase.TripField
import com.travelmate.ui.screens.addactivity.AddActivityContent
import com.travelmate.ui.screens.addactivity.AddActivityUiState
import com.travelmate.ui.screens.cost.CostLine
import com.travelmate.ui.screens.cost.TripCostSummaryContent
import com.travelmate.ui.screens.cost.TripCostSummaryUiState
import com.travelmate.ui.screens.createtrip.CreateTripContent
import com.travelmate.ui.screens.createtrip.CreateTripUiState
import com.travelmate.ui.screens.destination.DestinationDetailsContent
import com.travelmate.ui.screens.destination.DestinationDetailsUiState
import com.travelmate.ui.screens.explore.ExploreContent
import com.travelmate.ui.screens.explore.ExploreUiState
import com.travelmate.ui.screens.itinerary.DailyItineraryContent
import com.travelmate.ui.screens.itinerary.DailyItineraryUiState
import com.travelmate.ui.screens.map.MapContent
import com.travelmate.ui.screens.map.MapUiState
import com.travelmate.ui.screens.saved.SavedContent
import com.travelmate.ui.screens.saved.SavedPlace
import com.travelmate.ui.screens.saved.SavedTab
import com.travelmate.ui.screens.saved.SavedUiState
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Previews of every screen with SAMPLE data (debug source set only, never in the release build).
 * Each screen shows its main state here; loading, empty and error states are previewed next to the screen code.
 */

private val previewUser = User(uid = "sample", name = "SAMPLE User", email = "sample@example.com")

// Explore

@ThemePreviews
@Composable
private fun ExplorePreview() = TravelMateTheme {
    val all = listOf(PreviewData.destinationA, PreviewData.destinationB)
    ExploreContent(
        state = ExploreUiState(all = all, filtered = all, favoriteIds = setOf(PreviewData.destinationA.id), loading = false, user = previewUser),
        listState = rememberLazyListState(),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenDestination = {},
        onPlanTrip = {},
        onOpenProfile = {},
    )
}

@ThemePreviews
@Composable
private fun ExploreNoResultsPreview() = TravelMateTheme {
    ExploreContent(
        state = ExploreUiState(query = "zzz", loading = false, user = previewUser),
        listState = rememberLazyListState(),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenDestination = {},
        onPlanTrip = {},
        onOpenProfile = {},
    )
}

@ThemePreviews
@Composable
private fun ExploreErrorPreview() = TravelMateTheme {
    ExploreContent(
        state = ExploreUiState(loading = false, error = "SAMPLE error: permission denied", user = previewUser),
        listState = rememberLazyListState(),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenDestination = {},
        onPlanTrip = {},
        onOpenProfile = {},
    )
}

@ThemePreviews
@Composable
private fun ExploreLoadingPreview() = TravelMateTheme {
    ExploreContent(
        state = ExploreUiState(loading = true, user = previewUser),
        listState = rememberLazyListState(),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenDestination = {},
        onPlanTrip = {},
        onOpenProfile = {},
    )
}

// Destination Details

@ThemePreviews
@Preview(heightDp = 1400)
@Composable
private fun DestinationDetailsPreview() = TravelMateTheme {
    AmbientBackground {
        DestinationDetailsContent(
            state = DestinationDetailsUiState(
                destination = PreviewData.destinationA,
                places = PreviewData.places,
                loading = false,
                tipsExpanded = true,
                isFavorite = true,
            ),
            destination = PreviewData.destinationA,
            onEvent = {},
            onBack = {},
            onPlanTrip = {},
        )
    }
}

// Create Trip

@ThemePreviews
@Composable
private fun CreateTripPreview() = TravelMateTheme {
    CreateTripContent(
        state = CreateTripUiState(
            destinationId = PreviewData.destinationA.id,
            startDate = PreviewData.trip.startDate,
            destination = PreviewData.destinationA,
            destinations = listOf(PreviewData.destinationA, PreviewData.destinationB),
            placesLoaded = true,
            budgetText = "400",
            suggestedBudgetSar = 400,
            fixedCostSar = 230,
        ),
        onEvent = {},
        onBack = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}

@ThemePreviews
@Composable
private fun CreateTripBudgetTooLowPreview() = TravelMateTheme {
    CreateTripContent(
        state = CreateTripUiState(
            destinationId = PreviewData.destinationA.id,
            startDate = PreviewData.trip.startDate,
            destination = PreviewData.destinationA,
            destinations = listOf(PreviewData.destinationA),
            placesLoaded = true,
            budgetText = "100",
            budgetEdited = true,
            suggestedBudgetSar = 400,
            fixedCostSar = 230,
            submitted = true,
            errors = mapOf(TripField.BUDGET to "Too low for stay and transport. Minimum SAR 230."),
        ),
        onEvent = {},
        onBack = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}

@ThemePreviews
@Composable
private fun CreateTripLoadingPreview() = TravelMateTheme {
    CreateTripContent(
        state = CreateTripUiState(destinationId = PreviewData.destinationA.id, startDate = PreviewData.trip.startDate),
        onEvent = {},
        onBack = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}

// Daily Itinerary

private fun itineraryState(day: Int) = DailyItineraryUiState(
    loading = false,
    trip = PreviewData.trip,
    destination = PreviewData.destinationA,
    placesById = PreviewData.places.associateBy { it.id },
    selectedDay = day,
    cost = PreviewData.tripCost,
    dayCosts = mapOf(1 to 130, 2 to 50, 3 to 10),
)

@ThemePreviews
@Composable
private fun DailyItineraryPreview() = TravelMateTheme {
    AmbientBackground {
        DailyItineraryContent(
            state = itineraryState(day = 1),
            trip = PreviewData.trip,
            onEvent = {},
            onBack = {},
            onOpenMap = {},
            onOpenCostSummary = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@ThemePreviews
@Composable
private fun DailyItineraryEmptyDayPreview() = TravelMateTheme {
    AmbientBackground {
        DailyItineraryContent(
            state = itineraryState(day = 3),
            trip = PreviewData.trip,
            onEvent = {},
            onBack = {},
            onOpenMap = {},
            onOpenCostSummary = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

// Add Activity sheet

@ThemePreviews
@Composable
private fun AddActivityPreview() = TravelMateTheme {
    AmbientBackground {
        AddActivityContent(
            state = AddActivityUiState(loading = false, day = 2, candidates = PreviewData.places.take(4), addedCount = 1),
            onEvent = {},
        )
    }
}

// Map

@ThemePreviews
@Composable
private fun MapPreview() = TravelMateTheme {
    MapContent(
        state = MapUiState(
            loading = false,
            trip = PreviewData.trip,
            destination = PreviewData.destinationA,
            placesById = PreviewData.places.associateBy { it.id },
        ),
        onEvent = {},
        onBack = {},
        mapAvailable = false,
    )
}

// Trip Cost Summary

@ThemePreviews
@Preview(heightDp = 1000)
@Composable
private fun TripCostSummaryPreview() = TravelMateTheme {
    val cost = PreviewData.tripCost
    TripCostSummaryContent(
        state = TripCostSummaryUiState(
            loading = false,
            trip = PreviewData.trip,
            destination = PreviewData.destinationA,
            cost = cost,
            lines = listOf(
                CostLine(ExpenseCategory.FLIGHTS, 0, 100),
                CostLine(ExpenseCategory.STAY, cost.accommodationSar, 0),
                CostLine(ExpenseCategory.FOOD, cost.foodSar, 0),
                CostLine(ExpenseCategory.TRANSPORT, cost.transportSar, 0),
                CostLine(ExpenseCategory.ACTIVITIES, cost.activitiesSar, 0),
            ),
            stayNote = "SAR 100 x 2 nights x 1 room",
        ),
        onEvent = {},
        onBack = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}

// Saved

@ThemePreviews
@Composable
private fun SavedTripsPreview() = TravelMateTheme {
    SavedContent(
        state = SavedUiState(trips = listOf(PreviewData.trip), tripsLoading = false, favoritesLoading = false),
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
private fun SavedDestinationsPreview() = TravelMateTheme {
    SavedContent(
        state = SavedUiState(
            tab = SavedTab.DESTINATIONS,
            tripsLoading = false,
            favoritesLoading = false,
            destinations = listOf(PreviewData.destinationA),
            places = listOf(SavedPlace(PreviewData.places[1], PreviewData.destinationA.name)),
        ),
        contentPadding = PaddingValues(),
        onEvent = {},
        onOpenTrip = {},
        onOpenDestination = {},
        onExplore = {},
        snackbarHostState = remember { SnackbarHostState() },
    )
}
