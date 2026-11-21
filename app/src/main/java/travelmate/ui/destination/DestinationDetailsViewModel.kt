package com.travelmate.ui.screens.destination

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.FavoriteRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.usecase.PlanningRules
import com.travelmate.ui.navigation.NavArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DestinationDetailsUiState(
    val destination: Destination? = null,
    val places: List<Place> = emptyList(),
    /** null= All. */
    val filter: PlaceType? = null,
    val aboutExpanded: Boolean = false,
    val tipsExpanded: Boolean = false,
    val selectedPlace: Place? = null,
    val isFavorite: Boolean = false,
    val favoritePlaceIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val error: String? = null,
) {
    val filteredPlaces: List<Place>
        get() = if (filter == null) places else places.filter { it.type == filter }
}

sealed interface DestinationDetailsEvent {
    data class FilterChanged(val filter: PlaceType?) : DestinationDetailsEvent
    data object ToggleAbout : DestinationDetailsEvent
    data object ToggleTips : DestinationDetailsEvent
    data class PlaceSelected(val place: Place?) : DestinationDetailsEvent
    data object ToggleFavorite : DestinationDetailsEvent
    data class TogglePlaceFavorite(val place: Place) : DestinationDetailsEvent
}

/** Route argument: destinationId (destination/{destinationId}). */
class DestinationDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    catalogRepository: CatalogRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    val destinationId: String = checkNotNull(savedStateHandle[NavArgs.DESTINATION_ID])

    private val _state = MutableStateFlow(DestinationDetailsUiState())
    val state: StateFlow<DestinationDetailsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                catalogRepository.destination(destinationId),
                catalogRepository.places(destinationId),
            ) { destination, places -> destination to places.sortedWith(PlanningRules.placeOrder) }
                .catch { t -> _state.update { it.copy(loading = false, error = ErrorMessages.userMessage(t)) } }
                .collect { (destination, places) ->
                    _state.update {
                        it.copy(
                            destination = destination,
                            places = places,
                            loading = false,
                            notFound = destination == null,
                            error = null,
                        )
                    }
                }
        }
        viewModelScope.launch {
            favoriteRepository.favoriteDestinations()
                .catch { }
                .collect { refs -> _state.update { it.copy(isFavorite = refs.any { ref -> ref.id == destinationId }) } }
        }
        viewModelScope.launch {
            favoriteRepository.favoritePlaces()
                .catch { }
                .collect { refs -> _state.update { it.copy(favoritePlaceIds = refs.map { ref -> ref.id }.toSet()) } }
        }
    }

    fun onEvent(event: DestinationDetailsEvent) {
        when (event) {
            is DestinationDetailsEvent.FilterChanged -> _state.update { it.copy(filter = event.filter) }
            DestinationDetailsEvent.ToggleAbout -> _state.update { it.copy(aboutExpanded = !it.aboutExpanded) }
            DestinationDetailsEvent.ToggleTips -> _state.update { it.copy(tipsExpanded = !it.tipsExpanded) }
            is DestinationDetailsEvent.PlaceSelected -> _state.update { it.copy(selectedPlace = event.place) }
            DestinationDetailsEvent.ToggleFavorite -> {
                val favorite = !_state.value.isFavorite
                viewModelScope.launch { favoriteRepository.setDestinationFavorite(destinationId, favorite) }
            }
            is DestinationDetailsEvent.TogglePlaceFavorite -> {
                val favorite = event.place.id !in _state.value.favoritePlaceIds
                viewModelScope.launch {
                    favoriteRepository.setPlaceFavorite(event.place.id, event.place.destinationId, favorite)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                DestinationDetailsViewModel(createSavedStateHandle(), c.catalogRepository, c.favoriteRepository)
            }
        }
    }
}
