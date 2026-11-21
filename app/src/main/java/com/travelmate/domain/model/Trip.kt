package com.travelmate.domain.model

import java.time.LocalDate

data class ItineraryItem(
    val id: String,
    val placeId: String,
    val type: PlaceType,
    /** 1-based day of the trip. */
    val day: Int,
    /** 0-based position within the day. */
    val order: Int,
    /** Minutes after midnight. */
    val startMin: Int,
    val endMin: Int,
    /** Per person, snapshot when the item was added. */
    val priceSar: Int,
)

data class Trip(
    val id: String,
    val destinationId: String,
    val destinationName: String,
    val heroImageUrl: String,
    val name: String,
    val startDate: LocalDate,
    val days: Int,
    val travelers: Int,
    val budgetSar: Int,
    val interests: Set<PlaceType>,
    val items: List<ItineraryItem>,
    val totalCostSar: Int,
    val placesCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    /** Costs the user added in Edit Expenses, oldest first. */
    val expenses: List<Expense> = emptyList(),
) {
    val endDate: LocalDate get() = startDate.plusDays((days - 1).toLong())
    fun itemsForDay(day: Int) = items.filter { it.day == day }.sortedBy { it.order }
}
