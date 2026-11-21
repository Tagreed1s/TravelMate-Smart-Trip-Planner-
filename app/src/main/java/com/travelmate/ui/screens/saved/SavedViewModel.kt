package com.travelmate.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.FavoriteRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.Trip
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

enum class SavedTab(val label: String) { TRIPS("Trips"), DESTINATIONS("Destinations") }

/** A saved place with the name of its destination, for the "Restaurant · AlUla" line. */
data class SavedPlace(val place: Place, val destinationName: String)

data class SavedUiState(
    val tab: SavedTab = SavedTab.TRIPS,
    val trips: List<Trip> = emptyList(),
    val tripsLoading: Boolean = true,
    val tripsError: String? = null,
    val destinations: List<Destination> = emptyList(),
    val places: List<SavedPlace> = emptyList(),
    val favoritesLoading: Boolean = true,
    val favoritesError: String? = null,
    /** Trip waiting for the delete confirmation, or null. */
    val tripToDelete: Trip? = null,
)

sealed interface SavedEvent {
    data class TabSelected(val tab: SavedTab) : SavedEvent
    data class DeleteTripClicked(val trip: Trip) : SavedEvent
    data object DeleteTripDismissed : SavedEvent
    data object DeleteTripConfirmed : SavedEvent
    data class RemoveDestination(val destinationId: String) : SavedEvent
    data class RemovePlace(val placeId: String, val destinationId: String) : SavedEvent
}

sealed interface SavedEffect {
    data class ShowMessage(val text: String) : SavedEffect
}

/** Saved screen (instructor's change request): Saved Trips and Favorites merged into two tabs. */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModel(
    private val tripRepository: TripRepository,
    private val favoriteRepository: FavoriteRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedUiState())
    val state: StateFlow<SavedUiState> = _state.asStateFlow()

    private val _effects = Channel<SavedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            tripRepository.trips()
                .catch { t -> _state.update { it.copy(tripsLoading = false, tripsError = ErrorMessages.userMessage(t)) } }
                .collect { trips -> _state.update { it.copy(trips = trips, tripsLoading = false, tripsError = null) } }
        }
        viewModelScope.launch {
            combine(
                favoriteRepository.favoriteDestinations(),
                favoriteRepository.favoritePlaces(),
                catalogRepository.destinations(),
            ) { destinationRefs, placeRefs, allDestinations -> Triple(destinationRefs, placeRefs, allDestinations) }
                .flatMapLatest { (destinationRefs, placeRefs, allDestinations) ->
                    val destinationsById = allDestinations.associateBy { it.id }
                    val savedDestinations = destinationRefs.mapNotNull { destinationsById[it.id] }
                    val destinationIds = placeRefs.map { it.destinationId }.distinct()
                    if (destinationIds.isEmpty()) {
                        flowOf(savedDestinations to emptyList<SavedPlace>())
                    } else {
                        // Places are stored per destination, so read the places of every destination involved.
                        combine(destinationIds.map { catalogRepository.places(it) }) { lists ->
                            val placesById = lists.flatMap { it }.associateBy { it.id }
                            val savedPlaces = placeRefs.mapNotNull { ref ->
                                placesById[ref.id]?.let { place ->
                                    SavedPlace(place, destinationsById[place.destinationId]?.name.orEmpty())
                                }
                            }
                            savedDestinations to savedPlaces
                        }
                    }
                }
                .catch { t -> _state.update { it.copy(favoritesLoading = false, favoritesError = ErrorMessages.userMessage(t)) } }
                .collect { (destinations, places) ->
                    _state.update {
                        it.copy(destinations = destinations, places = places, favoritesLoading = false, favoritesError = null)
                    }
                }
        }
    }

    fun onEvent(event: SavedEvent) {
        when (event) {
            is SavedEvent.TabSelected -> _state.update { it.copy(tab = event.tab) }
            is SavedEvent.DeleteTripClicked -> _state.update { it.copy(tripToDelete = event.trip) }
            SavedEvent.DeleteTripDismissed -> _state.update { it.copy(tripToDelete = null) }
            SavedEvent.DeleteTripConfirmed -> deleteTrip()
            is SavedEvent.RemoveDestination -> viewModelScope.launch {
                favoriteRepository.setDestinationFavorite(event.destinationId, favorite = false).reportFailure()
            }
            is SavedEvent.RemovePlace -> viewModelScope.launch {
                favoriteRepository.setPlaceFavorite(event.placeId, event.destinationId, favorite = false).reportFailure()
            }
        }
    }

    private fun deleteTrip() {
        val trip = _state.value.tripToDelete ?: return
        _state.update { it.copy(tripToDelete = null) }
        viewModelScope.launch { tripRepository.deleteTrip(trip.id).reportFailure() }
    }

    private suspend fun Result<Unit>.reportFailure() {
        onFailure { t -> _effects.send(SavedEffect.ShowMessage(ErrorMessages.userMessage(t))) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                SavedViewModel(c.tripRepository, c.favoriteRepository, c.catalogRepository)
            }
        }
    }
}
