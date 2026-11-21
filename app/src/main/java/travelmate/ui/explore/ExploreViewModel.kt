package com.travelmate.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.core.ErrorMessages
import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.FavoriteRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val query: String = "",
    val all: List<Destination> = emptyList(),
    val filtered: List<Destination> = emptyList(),
    /**Ids of the destinations the user saved (filled hearts). */
    val favoriteIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val error: String? = null,
    val user: User? = null,
)

sealed interface ExploreEvent {
    data class QueryChanged(val query: String) : ExploreEvent
    data object ClearQuery : ExploreEvent
    data class ToggleFavorite(val destinationId: String) : ExploreEvent
    data object Retry : ExploreEvent
}

/** Name or country contains the query, ignoring case. A blank query keeps everything. */
fun filterDestinations(all: List<Destination>, query: String): List<Destination> {
    val q = query.trim()
    if (q.isEmpty()) return all
    return all.filter { it.name.contains(q, ignoreCase = true) || it.country.contains(q, ignoreCase = true) }
}

class ExploreViewModel(
    private val catalogRepository: CatalogRepository,
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    private var catalogJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user -> _state.update { it.copy(user = user) } }
        }
        viewModelScope.launch {
            favoriteRepository.favoriteDestinations()
                .catch { /* Hearts simply stay empty if favorites cannot be read. */ }
                .collect { refs -> _state.update { it.copy(favoriteIds = refs.map { ref -> ref.id }.toSet()) } }
        }
        loadCatalog()
    }

    fun onEvent(event: ExploreEvent) {
        when (event) {
            is ExploreEvent.QueryChanged -> _state.update {
                it.copy(query = event.query, filtered = filterDestinations(it.all, event.query))
            }
            ExploreEvent.ClearQuery -> _state.update { it.copy(query = "", filtered = it.all) }
            is ExploreEvent.ToggleFavorite -> {
                val favorite = event.destinationId !in _state.value.favoriteIds
                viewModelScope.launch { favoriteRepository.setDestinationFavorite(event.destinationId, favorite) }
            }
            ExploreEvent.Retry -> loadCatalog()
        }
    }

    /** (Re)subscribes to the catalog; used at start and by the Retry button of the error state. */
    private fun loadCatalog() {
        catalogJob?.cancel()
        _state.update { it.copy(loading = it.all.isEmpty(), error = null) }
        catalogJob = viewModelScope.launch {
            catalogRepository.destinations()
                .catch { t -> _state.update { it.copy(loading = false, error = ErrorMessages.userMessage(t)) } }
                .collect { list ->
                    _state.update {
                        it.copy(all = list, filtered = filterDestinations(list, it.query), loading = false, error = null)
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ExploreViewModel(container.catalogRepository, container.authRepository, container.favoriteRepository)
            }
        }
    }
}
