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

// Add Activity
import com.travelmate.ui.addactivity.AddActivityContent
import com.travelmate.ui.addactivity.AddActivityUiState

// Cost
import com.travelmate.ui.screens.cost.CostLine
import com.travelmate.ui.screens.cost.TripCostSummaryContent
import com.travelmate.ui.screens.cost.TripCostSummaryUiState

// Create Trip
import com.travelmate.ui.screens.createtrips.CreateTripContent
import com.travelmate.ui.screens.createtrips.CreateTripUiState

// Destination Details
import com.travelmate.ui.screens.destination.DestinationDetailsContent
import com.travelmate.ui.screens.destination.DestinationDetailsUiState

// Explore
import com.travelmate.ui.screens.explore.ExploreContent
import com.travelmate.ui.screens.explore.ExploreUiState

// Itinerary
import com.travelmate.ui.screens.itinerary.DailyItineraryContent
import com.travelmate.ui.screens.itinerary.DailyItineraryUiState

// Map
import com.travelmate.ui.map.MapContent
import com.travelmate.ui.map.MapUiState

// Saved
import com.travelmate.ui.screens.saved.SavedContent
import com.travelmate.ui.screens.saved.SavedPlace
import com.travelmate.ui.screens.saved.SavedTab
import com.travelmate.ui.screens.saved.SavedUiState

// Theme
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme