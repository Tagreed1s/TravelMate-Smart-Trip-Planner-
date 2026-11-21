package com.travelmate.data.firebase

/** Every Realtime Database path the app uses (plan Section 6.1). */
object FirebasePaths {
    const val CATALOG = "catalog"
    const val DESTINATIONS = "catalog/destinations"
    fun places(destinationId: String) = "catalog/places/$destinationId"
    fun profile(uid: String) = "users/$uid/profile"
    fun trips(uid: String) = "users/$uid/trips"
    fun trip(uid: String, tripId: String) = "users/$uid/trips/$tripId"
    fun favoriteDestinations(uid: String) = "users/$uid/favoriteDestinations"
    fun favoritePlaces(uid: String) = "users/$uid/favoritePlaces"
}
