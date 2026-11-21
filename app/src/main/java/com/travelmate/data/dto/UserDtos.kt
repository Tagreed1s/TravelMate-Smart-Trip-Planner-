package com.travelmate.data.dto

/** Mirrors /users/{uid}/profile. */
data class ProfileDto(
    var name: String = "",
    var email: String = "",
    var createdAt: Long = 0,
)

/** Mirrors /users/{uid}/trips/{tripId} (plan Section 6.5). Read with getValue; written as a map (Trip.toWriteMap in Mappers.kt). */
data class TripDto(
    var id: String = "",
    var destinationId: String = "",
    var destinationName: String = "",
    var heroImageUrl: String = "",
    var name: String = "",
    var startDate: String = "",
    var days: Int = 0,
    var travelers: Int = 0,
    var budgetSar: Int = 0,
    var interests: List<String> = emptyList(),
    var items: Map<String, ItemDto> = emptyMap(),
    var totalCostSar: Int = 0,
    var placesCount: Int = 0,
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
    var expenses: Map<String, ExpenseDto> = emptyMap(),
)

/** Mirrors /users/{uid}/trips/{tripId}/items/{itemId}. */
data class ItemDto(
    var id: String = "",
    var placeId: String = "",
    var type: String = "",
    var day: Int = 0,
    var order: Int = 0,
    var startMin: Int = 0,
    var endMin: Int = 0,
    var priceSar: Int = 0,
)

/** Mirrors /users/{uid}/trips/{tripId}/expenses/{expenseId}. */
data class ExpenseDto(
    var id: String = "",
    var category: String = "",
    var title: String = "",
    var amountSar: Int = 0,
    var createdAt: Long = 0,
)

/** Mirrors /users/{uid}/favoriteDestinations/{destinationId} and /users/{uid}/favoritePlaces/{placeId}. */
data class FavoriteDto(
    var destinationId: String = "",
    var addedAt: Long = 0,
)
