package com.travelmate.ui.screens.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelmate.ui.components.DestinationCard
import com.travelmate.ui.components.EmptyState
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.components.TMTopBar
import com.travelmate.ui.theme.AmbientBackground
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Route: explore (home tab). Destination list from Firebase with live search.
 * "Plan" on a card opens Create Trip; the avatar opens Profile.
 */
@Composable
fun ExploreScreen(
    contentPadding: PaddingValues,
    onOpenDestination: (String) -> Unit,
    onPlanTrip: (String) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Hoisted so the scroll position survives tab switches and loading states.
    val listState = rememberLazyListState()

    ExploreContent(
        state = state,
        listState = listState,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onOpenDestination = onOpenDestination,
        onPlanTrip = onPlanTrip,
        onOpenProfile = onOpenProfile,
    )
}

@Composable
fun ExploreContent(
    state: ExploreUiState,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onEvent: (ExploreEvent) -> Unit,
    onOpenDestination: (String) -> Unit,
    onPlanTrip: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    AmbientBackground {
        Column(Modifier.fillMaxSize()) {
            TMTopBar(
                title = "Hi, ${state.user?.firstName ?: "traveler"}",
                avatarInitials = state.user?.initials ?: "?",
                onAvatarClick = onOpenProfile,
            )
            TMTextField(
                value = state.query,
                onValueChange = { onEvent(ExploreEvent.QueryChanged(it)) },
                placeholder = "Where to?",
                leadingIcon = Icons.Rounded.Search,
                trailingIcon = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onEvent(ExploreEvent.ClearQuery) }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                        }
                    }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.padding(horizontal = Spacing.gutter),
            )
            Box(Modifier.weight(1f)) {
                when {
                    state.loading -> CircularProgressIndicator(
                        color = TravelMateTheme.colors.primary,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.error != null -> EmptyState(
                        icon = Icons.Rounded.CloudOff,
                        title = "Could not load destinations",
                        message = state.error,
                        actionLabel = "Try again",
                        onAction = { onEvent(ExploreEvent.Retry) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.filtered.isEmpty() -> EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = "No destinations match",
                        message = "Try another name or country.",
                        actionLabel = if (state.query.isNotEmpty()) "Clear search" else null,
                        onAction = if (state.query.isNotEmpty()) ({ onEvent(ExploreEvent.ClearQuery) }) else null,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = Spacing.gutter,
                            end = Spacing.gutter,
                            top = Spacing.lg,
                            bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        items(state.filtered, key = { it.id }) { destination ->
                            DestinationCard(
                                destination = destination,
                                onClick = { onOpenDestination(destination.id) },
                                onPlan = { onPlanTrip(destination.id) },
                                isFavorite = destination.id in state.favoriteIds,
                                onToggleFavorite = { onEvent(ExploreEvent.ToggleFavorite(destination.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}
