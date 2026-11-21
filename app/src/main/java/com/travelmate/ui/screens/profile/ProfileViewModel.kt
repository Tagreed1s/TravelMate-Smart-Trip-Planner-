package com.travelmate.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.repository.FavoriteRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.di.appContainer
import com.travelmate.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    /** null while loading or when the count could not be read. */
    val tripsCount: Int? = null,
    val savedCount: Int? = null,
    val showLogoutConfirm: Boolean = false,
)

sealed interface ProfileEvent {
    data object LogoutClicked : ProfileEvent
    data object LogoutDismissed : ProfileEvent
    data object LogoutConfirmed : ProfileEvent
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    tripRepository: TripRepository,
    favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user -> _state.update { it.copy(user = user) } }
        }
        viewModelScope.launch {
            tripRepository.trips()
                .catch { }
                .collect { trips -> _state.update { it.copy(tripsCount = trips.size) } }
        }
        viewModelScope.launch {
            combine(favoriteRepository.favoriteDestinations(), favoriteRepository.favoritePlaces()) { d, p -> d.size + p.size }
                .catch { }
                .collect { count -> _state.update { it.copy(savedCount = count) } }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.LogoutClicked -> _state.update { it.copy(showLogoutConfirm = true) }
            ProfileEvent.LogoutDismissed -> _state.update { it.copy(showLogoutConfirm = false) }
            ProfileEvent.LogoutConfirmed -> {
                _state.update { it.copy(showLogoutConfirm = false) }
                // The NavHost sees the user become null and opens Login with a cleared back stack.
                authRepository.signOut()
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                ProfileViewModel(c.authRepository, c.tripRepository, c.favoriteRepository)
            }
        }
    }
}
