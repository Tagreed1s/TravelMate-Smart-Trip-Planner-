package com.travelmate.domain.usecase

import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.TripCost
import kotlin.math.max

/**
 * Trip cost rules (plan Section 9.2):
 * - stay = price per night x nights x rooms, where nights = days - 1 and rooms = travelers / 2 rounded up
 * - transport = price per day x days (per group)
 * - activities and food = item price per person x travelers, split by RESTAURANT or not
 * - extras = the user's own expenses (whole group), for example flights
 */
class CalculateTripCostUseCase {

    operator fun invoke(
        destination: Destination,
        days: Int,
        travelers: Int,
        items: List<ItineraryItem>,
        expenses: List<Expense> = emptyList(),
    ): TripCost = TripCost(
        accommodationSar = accommodation(destination, days, travelers),
        transportSar = transport(destination, days),
        activitiesSar = items.filter { it.type != PlaceType.RESTAURANT }.sumOf { it.priceSar * travelers },
        foodSar = items.filter { it.type == PlaceType.RESTAURANT }.sumOf { it.priceSar * travelers },
        extrasSar = expenses.sumOf { it.amountSar },
    )

    fun nights(days: Int): Int = max(days - 1, 0)

    fun rooms(travelers: Int): Int =
        (travelers + PlanningRules.TRAVELERS_PER_ROOM - 1) / PlanningRules.TRAVELERS_PER_ROOM

    fun accommodation(destination: Destination, days: Int, travelers: Int): Int =
        destination.accommodationPerNightSar * nights(days) * rooms(travelers)

    fun transport(destination: Destination, days: Int): Int = destination.transportPerDaySar * days

    /** Stay + transport: what the trip costs before any place is added. */
    fun fixedCost(destination: Destination, days: Int, travelers: Int): Int =
        accommodation(destination, days, travelers) + transport(destination, days)

    /** Cost shown in a day header: that day's transport plus its places for all travelers. */
    fun dayCost(dayItems: List<ItineraryItem>, travelers: Int, destination: Destination): Int =
        destination.transportPerDaySar + dayItems.sumOf { it.priceSar * travelers }
}
