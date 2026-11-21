package com.travelmate.ui.screens.itinerary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.domain.usecase.EditItineraryUseCase
import com.travelmate.ui.navigation.NavArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DailyItineraryUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val error: String? = null,
    val trip: Trip? = null,
    val destination: Destination? = null,
    val placesById: Map<String, Place> = emptyMap(),
    val selectedDay: Int = 1,
    /** Whole trip, including the user's expenses (budget bar). */
    val cost: TripCost? = null,
    /** Day number -> that day's transport plus its places. */
    val dayCosts: Map<Int, Int> = emptyMap(),
    val showAddActivity: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    /** True while the trip is being deleted; the screen closes when it is done. */
    val deleting: Boolean = false,
)

sealed interface DailyItineraryEvent {
    data class DaySelected(val day: Int) : DailyItineraryEvent
    data class RemoveItem(val itemId: String) : DailyItineraryEvent
    data object AddActivityClicked : DailyItineraryEvent
    data object AddActivityDismissed : DailyItineraryEvent
    data object RenameClicked : DailyItineraryEvent
    data object RenameDismissed : DailyItineraryEvent
    data class RenameConfirmed(val name: String) : DailyItineraryEvent
    data object DeleteClicked : DailyItineraryEvent
    data object DeleteDismissed : DailyItineraryEvent
    data object DeleteConfirmed : DailyItineraryEvent
}

sealed interface DailyItineraryEffect {
    data class ShowMessage(val text: String) : DailyItineraryEffect
    data object TripDeleted : DailyItineraryEffect
}

private data class TripData(val trip: Trip?, val destination: Destination?, val places: List<Place>)

/** Route argument: tripId (daily_itinerary/{tripId}). */
@OptIn(ExperimentalCoroutinesApi::class)
class DailyItineraryViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val tripRepository: TripRepository,
    private val catalogRepository: CatalogRepository,
    private val calculateTripCost: CalculateTripCostUseCase,
    private val editItinerary: EditItineraryUseCase,
) : ViewModel() {

    val tripId: String = checkNotNull(savedStateHandle[NavArgs.TRIP_ID])

    // The selected day lives in SavedStateHandle, so it survives rotation and process death.
    private val _state = MutableStateFlow(DailyItineraryUiState(selectedDay = savedStateHandle[KEY_SELECTED_DAY] ?: 1))
    val state: StateFlow<DailyItineraryUiState> = _state.asStateFlow()

    private val _effects = Channel<DailyItineraryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            tripRepository.trip(tripId)
                .flatMapLatest { trip ->
                    if (trip == null) {
                        flowOf(TripData(null, null, emptyList()))
                    } else {
                        combine(
                            catalogRepository.destination(trip.destinationId).catch { emit(null) },
                            catalogRepository.places(trip.destinationId).catch { emit(emptyList()) },
                        ) { destination, places -> TripData(trip, destination, places) }
                    }
                }
                .catch { t -> _state.update { it.copy(loading = false, error = ErrorMessages.userMessage(t)) } }
                .collect { data ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = null,
                            notFound = data.trip == null,
                            trip = data.trip,
                            destination = data.destination,
                            placesById = data.places.associateBy { place -> place.id },
                            selectedDay = it.selectedDay.coerceIn(1, maxOf(data.trip?.days ?: 1, 1)),
                        ).withDerivedValues()
                    }
                }
        }
    }

    fun onEvent(event: DailyItineraryEvent) {
        when (event) {
            is DailyItineraryEvent.DaySelected -> {
                savedStateHandle[KEY_SELECTED_DAY] = event.day
                _state.update { it.copy(selectedDay = event.day) }
            }
            is DailyItineraryEvent.RemoveItem -> removeItem(event.itemId)
            DailyItineraryEvent.AddActivityClicked -> _state.update { it.copy(showAddActivity = true) }
            DailyItineraryEvent.AddActivityDismissed -> _state.update { it.copy(showAddActivity = false) }
            DailyItineraryEvent.RenameClicked -> _state.update { it.copy(showRenameDialog = true) }
            DailyItineraryEvent.RenameDismissed -> _state.update { it.copy(showRenameDialog = false) }
            is DailyItineraryEvent.RenameConfirmed -> rename(event.name)
            DailyItineraryEvent.DeleteClicked -> _state.update { it.copy(showDeleteConfirm = true) }
            DailyItineraryEvent.DeleteDismissed -> _state.update { it.copy(showDeleteConfirm = false) }
            DailyItineraryEvent.DeleteConfirmed -> delete()
        }
    }

    /** Drop the item, re-time its day, update the screen at once, then save in the background. */
    private fun removeItem(itemId: String) {
        val current = _state.value
        val trip = current.trip ?: return
        val destination = current.destination ?: return
        val durations = current.placesById.mapValues { (_, place) -> place.durationMin }
        val result = editItinerary.remove(trip, itemId, destination, durations) ?: return
        val updatedTrip = trip.copy(
            items = result.items,
            totalCostSar = result.cost.totalSar,
            placesCount = result.items.map { it.placeId }.distinct().size,
        )
        _state.update { it.copy(trip = updatedTrip).withDerivedValues() }

        viewModelScope.launch {
            tripRepository.replaceItems(trip.id, result.items, result.cost)
                .onFailure { t -> _effects.send(DailyItineraryEffect.ShowMessage(ErrorMessages.userMessage(t))) }
        }
    }

    private fun rename(name: String) {
        _state.update { it.copy(showRenameDialog = false) }
        if (name.isBlank()) return
        viewModelScope.launch {
            tripRepository.renameTrip(tripId, name)
                .onFailure { t -> _effects.send(DailyItineraryEffect.ShowMessage(ErrorMessages.userMessage(t))) }
        }
    }

    private fun delete() {
        _state.update { it.copy(showDeleteConfirm = false, deleting = true) }
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
                .onSuccess { _effects.send(DailyItineraryEffect.TripDeleted) }
                .onFailure { t ->
                    _state.update { it.copy(deleting = false) }
                    _effects.send(DailyItineraryEffect.ShowMessage(ErrorMessages.userMessage(t)))
                }
        }
    }

    private fun DailyItineraryUiState.withDerivedValues(): DailyItineraryUiState {
        val trip = trip
        val destination = destination
        if (trip == null || destination == null) return copy(cost = null, dayCosts = emptyMap())
        return copy(
            cost = calculateTripCost(destination, trip.days, trip.travelers, trip.items, trip.expenses),
            dayCosts = (1..trip.days).associateWith { day ->
                calculateTripCost.dayCost(trip.itemsForDay(day), trip.travelers, destination)
            },
        )
    }

    companion object {
        private const val KEY_SELECTED_DAY = "selectedDay"

        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                DailyItineraryViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    tripRepository = c.tripRepository,
                    catalogRepository = c.catalogRepository,
                    calculateTripCost = c.calculateTripCost,
                    editItinerary = c.editItinerary,
                )
            }
        }
    }
}
