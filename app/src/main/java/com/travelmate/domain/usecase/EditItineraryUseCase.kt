package com.travelmate.domain.usecase

import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.Trip
import com.travelmate.domain.model.TripCost

sealed interface ItineraryEdit {
    data class Updated(val items: List<ItineraryItem>, val cost: TripCost) : ItineraryEdit

    /** The place would end after 22:00, so it was not added. */
    data object DayFull : ItineraryEdit
}

/**
 * Adds or removes one place in a saved trip (Daily Itinerary and Add Activity sheet).
 * After every change the day is scheduled again (09:00 start, 60 minute gaps, nothing after 22:00),
 * so times always stay consistent, and the cost is recalculated including the user's expenses.
 */
class EditItineraryUseCase(
    private val scheduleDay: ScheduleDayUseCase,
    private val calculateTripCost: CalculateTripCostUseCase,
) {

    /**
     * Removes [itemId] and re-times its day. [durations] maps placeId to visit length; when a place is
     * missing from the catalog the item keeps its old length. Returns null when the item does not exist.
     */
    fun remove(
        trip: Trip,
        itemId: String,
        destination: Destination,
        durations: Map<String, Int>,
    ): ItineraryEdit.Updated? {
        val removed = trip.items.firstOrNull { it.id == itemId } ?: return null
        val day = removed.day
        val entries = trip.itemsForDay(day).filter { it.id != itemId }.map { it.toEntry(durations) }
        val items = trip.items.filter { it.day != day } + scheduleDay(day, entries).items
        return ItineraryEdit.Updated(items, cost(trip, destination, items))
    }

    /** Adds [place] at the end of [day]. */
    fun add(
        trip: Trip,
        day: Int,
        place: Place,
        newItemId: String,
        destination: Destination,
        durations: Map<String, Int>,
    ): ItineraryEdit {
        val entries = trip.itemsForDay(day).map { it.toEntry(durations) } + ScheduleEntry(
            id = newItemId,
            placeId = place.id,
            type = place.type,
            durationMin = place.durationMin,
            priceSar = place.priceSar,
        )
        val result = scheduleDay(day, entries)
        if (result.didNotFit.any { it.id == newItemId }) return ItineraryEdit.DayFull
        val items = trip.items.filter { it.day != day } + result.items
        return ItineraryEdit.Updated(items, cost(trip, destination, items))
    }

    private fun cost(trip: Trip, destination: Destination, items: List<ItineraryItem>): TripCost =
        calculateTripCost(destination, trip.days, trip.travelers, items, trip.expenses)

    private fun ItineraryItem.toEntry(durations: Map<String, Int>) = ScheduleEntry(
        id = id,
        placeId = placeId,
        type = type,
        durationMin = durations[placeId] ?: (endMin - startMin),
        priceSar = priceSar,
    )
}
