package com.travelmate.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.travelmate.TravelMateApp
import com.travelmate.data.repository.AuthRepository
import com.travelmate.data.repository.CatalogRepository
import com.travelmate.data.repository.FavoriteRepository
import com.travelmate.data.repository.FirebaseAuthRepository
import com.travelmate.data.repository.FirebaseCatalogRepository
import com.travelmate.data.repository.FirebaseFavoriteRepository
import com.travelmate.data.repository.FirebaseTripRepository
import com.travelmate.data.repository.TripRepository
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.domain.usecase.EditItineraryUseCase
import com.travelmate.domain.usecase.GenerateItineraryUseCase
import com.travelmate.domain.usecase.ScheduleDayUseCase
import com.travelmate.domain.usecase.SuggestBudgetUseCase
import com.travelmate.domain.usecase.ValidateTripInputUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual dependency injection: one instance per app, built in [TravelMateApp]. */
class AppContainer {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val authRepository: AuthRepository = FirebaseAuthRepository(Firebase.auth, Firebase.database)
    val catalogRepository: CatalogRepository = FirebaseCatalogRepository(Firebase.database, authRepository, appScope)
    val tripRepository: TripRepository = FirebaseTripRepository(Firebase.database, authRepository)
    val favoriteRepository: FavoriteRepository = FirebaseFavoriteRepository(Firebase.database, authRepository)
    val validateTripInput = ValidateTripInputUseCase()
    val scheduleDay = ScheduleDayUseCase()
    val calculateTripCost = CalculateTripCostUseCase()
    val suggestBudget = SuggestBudgetUseCase(calculateTripCost)
    val generateItinerary = GenerateItineraryUseCase(scheduleDay, calculateTripCost)
    val editItinerary = EditItineraryUseCase(scheduleDay, calculateTripCost)
}

/** Gives ViewModel initializers access to the container: `initializer { XViewModel(appContainer().repo) }`. */
fun CreationExtras.appContainer(): AppContainer = (this[APPLICATION_KEY] as TravelMateApp).container
