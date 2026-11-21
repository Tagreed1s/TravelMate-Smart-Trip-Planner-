package com.travelmate.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelmate.di.AppContainer
import com.travelmate.ui.screens.auth.ForgotPasswordScreen
import com.travelmate.ui.screens.auth.LoginScreen
import com.travelmate.ui.screens.auth.SignUpScreen
import com.travelmate.ui.screens.cost.TripCostSummaryScreen
import com.travelmate.ui.screens.createtrips.CreateTripScreen
import com.travelmate.ui.screens.destination.DestinationDetailsScreen
import com.travelmate.ui.screens.explore.ExploreScreen
import com.travelmate.ui.screens.itinerary.DailyItineraryScreen
import com.travelmate.ui.map.MapScreen
import com.travelmate.ui.screens.profile.ProfileScreen
import com.travelmate.ui.screens.saved.SavedScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The navigation graph: one composable() per screen route from [Screen].
 *
 * Auth gate: the app starts at Explore when a user is signed in, otherwise at Login.
 * When the user becomes null (log out), the back stack is cleared and Login is shown.
 */
@Composable
fun TravelMateNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val authRepository = container.authRepository
    val startDestination = remember {
        if (authRepository.currentUser.value != null) Screen.Explore.route else Screen.Login.route
    }

    LaunchedEffect(navController) {
        authRepository.currentUser.map { it != null }.distinctUntilChanged().collect { signedIn ->
            val currentRoute = navController.currentDestination?.route ?: return@collect
            val onAuthScreen = currentRoute in setOf(Screen.Login.route, Screen.SignUp.route, Screen.ForgotPassword.route)
            if (!signedIn && !onAuthScreen) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }

    // After log in or sign up: Explore becomes the only screen in the back stack.
    val goToExplore = {
        navController.navigate(Screen.Explore.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    MainScaffold(navController) { bottomBarPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) },
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onSignedIn = goToExplore,
                    onSignUp = { navController.navigate(Screen.SignUp.route) { launchSingleTop = true } },
                    onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) { launchSingleTop = true } },
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignedUp = goToExplore,
                    onLogIn = {
                        // Back to the Login screen when it is under Sign Up, otherwise open it.
                        if (!navController.popBackStack(Screen.Login.route, inclusive = false)) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    },
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    contentPadding = bottomBarPadding,
                    onOpenDestination = { navController.navigate(Screen.DestinationDetails.createRoute(it)) },
                    onPlanTrip = { navController.navigate(Screen.CreateTrip.createRoute(it)) },
                    onOpenProfile = { navController.navigateToTab(TopLevelTab.PROFILE) },
                )
            }
            composable(
                route = Screen.DestinationDetails.route,
                arguments = listOf(navArgument(NavArgs.DESTINATION_ID) { type = NavType.StringType }),
            ) {
                DestinationDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onPlanTrip = { navController.navigate(Screen.CreateTrip.createRoute(it)) },
                )
            }
            composable(
                route = Screen.CreateTrip.route,
                arguments = listOf(navArgument(NavArgs.DESTINATION_ID) { type = NavType.StringType }),
            ) {
                CreateTripScreen(
                    onBack = { navController.popBackStack() },
                    onTripCreated = { tripId ->
                        // Back from the new plan returns to where planning started, not to the form.
                        navController.navigate(Screen.DailyItinerary.createRoute(tripId)) {
                            popUpTo(Screen.CreateTrip.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Screen.DailyItinerary.route,
                arguments = listOf(navArgument(NavArgs.TRIP_ID) { type = NavType.StringType }),
            ) {
                DailyItineraryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenMap = { navController.navigate(Screen.TripMap.createRoute(it)) },
                    onOpenCostSummary = { navController.navigate(Screen.TripCostSummary.createRoute(it)) },
                )
            }
            composable(
                route = Screen.TripMap.route,
                arguments = listOf(navArgument(NavArgs.TRIP_ID) { type = NavType.StringType }),
            ) {
                MapScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.TripCostSummary.route,
                arguments = listOf(navArgument(NavArgs.TRIP_ID) { type = NavType.StringType }),
            ) {
                TripCostSummaryScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Saved.route) {
                SavedScreen(
                    contentPadding = bottomBarPadding,
                    onOpenTrip = { navController.navigate(Screen.DailyItinerary.createRoute(it)) },
                    onOpenDestination = { navController.navigate(Screen.DestinationDetails.createRoute(it)) },
                    onExplore = { navController.navigateToTab(TopLevelTab.EXPLORE) },
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(contentPadding = bottomBarPadding)
            }
        }
    }
}