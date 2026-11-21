package com.travelmate.ui.navigation

/** Names of the navigation arguments used inside routes. */
object NavArgs {
    const val DESTINATION_ID = "destinationId"
    const val TRIP_ID = "tripId"
}

/**
 * Every route of the app, defined in one place (snake_case, as asked in the instructor's change request).
 *
 * Screens that need an argument have a route function that builds the real route, for example
 * `Screen.DailyItinerary.createRoute("abc")` gives "daily_itinerary/abc".
 * The Add Activity and Edit Expenses sheets have no route: they open on top of their screens.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object ForgotPassword : Screen("forgot_password")
    data object Explore : Screen("explore")

    data object DestinationDetails : Screen("destination/{${NavArgs.DESTINATION_ID}}") {
        fun createRoute(destinationId: String) = "destination/$destinationId"
    }

    data object CreateTrip : Screen("create_trip/{${NavArgs.DESTINATION_ID}}") {
        fun createRoute(destinationId: String) = "create_trip/$destinationId"
    }

    data object DailyItinerary : Screen("daily_itinerary/{${NavArgs.TRIP_ID}}") {
        fun createRoute(tripId: String) = "daily_itinerary/$tripId"
    }

    data object TripMap : Screen("trip_map/{${NavArgs.TRIP_ID}}") {
        fun createRoute(tripId: String) = "trip_map/$tripId"
    }

    data object TripCostSummary : Screen("trip_cost_summary/{${NavArgs.TRIP_ID}}") {
        fun createRoute(tripId: String) = "trip_cost_summary/$tripId"
    }

    data object Saved : Screen("saved")
    data object Profile : Screen("profile")
}
