package com.travelmate.domain.usecase

import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.TripCost
import com.travelmate.domain.model.TripRequest
import com.travelmate.domain.util.Distance

sealed interface GenerationResult {
    data class Success(val items: List<ItineraryItem>, val cost: TripCost) : GenerationResult
    data class BudgetTooLow(val minimumSar: Int) : GenerationResult
}

/**
 * Builds a day-by-day plan from the catalog (plan Section 9.5).
 *
 * Each day has up to 4 slots: morning (best non-food place), lunch (restaurant),
 * afternoon (the non-food place nearest to the morning one) and dinner (only with the Restaurants interest).
 * The money left after stay and transport is shared evenly across the remaining days, so the plan
 * never goes over budget. Output is deterministic: ties use [PlanningRules.placeOrder].
 */
class GenerateItineraryUseCase(
    private val scheduleDay: ScheduleDayUseCase,
    private val calculateTripCost: CalculateTripCostUseCase,
) {

    /** [request] must already be valid (see [ValidateTripInputUseCase]). */
    operator fun invoke(
        destination: Destination,
        places: List<Place>,
        request: TripRequest,
        idFactory: () -> String,
    ): GenerationResult {
        val days = request.days
        val travelers = request.travelers
        val budget = requireNotNull(request.budgetSar) { "Validate the request before generating" }

        val fixed = calculateTripCost.fixedCost(destination, days, travelers)
        if (budget < fixed) return GenerationResult.BudgetTooLow(minimumSar = fixed)

        val itemBudget = budget - fixed
        val nonFood = places
            .filter { it.type in request.interests && it.type != PlaceType.RESTAURANT }
            .sortedWith(PlanningRules.placeOrder)
        val restaurants = places
            .filter { it.type == PlaceType.RESTAURANT }
            .sortedWith(PlanningRules.placeOrder)
        val includeDinner = PlaceType.RESTAURANT in request.interests

        val used = mutableSetOf<String>()
        val usedRestaurants = mutableSetOf<String>()
        var spent = 0
        val items = mutableListOf<ItineraryItem>()

        fun cost(p: Place) = p.priceSar * travelers

        for (day in 1..days) {
            val allowance = (itemBudget - spent) / (days - day + 1)
            var daySpent = 0
            val todaysRestaurants = mutableSetOf<String>()

            fun fits(p: Place) = daySpent + cost(p) <= allowance

            fun pickRestaurant(): Place? {
                var r = restaurants.firstOrNull { it.id !in usedRestaurants && fits(it) }
                if (r == null && usedRestaurants.isNotEmpty()) {
                    // Repeats are allowed once all were used; never the same restaurant twice in one day.
                    usedRestaurants.clear()
                    r = restaurants.firstOrNull { it.id !in todaysRestaurants && fits(it) }
                }
                if (r != null) {
                    usedRestaurants += r.id
                    todaysRestaurants += r.id
                    daySpent += cost(r)
                }
                return r
            }

            val morning = nonFood.firstOrNull { it.id !in used && fits(it) }
                ?.also { used += it.id; daySpent += cost(it) }
            val lunch = pickRestaurant()

            val anchorLat = morning?.lat ?: destination.lat
            val anchorLng = morning?.lng ?: destination.lng
            val afternoon = nonFood
                .filter { it.id !in used && fits(it) }
                .minWithOrNull(
                    compareBy<Place> { Distance.km(anchorLat, anchorLng, it.lat, it.lng) }
                        .then(PlanningRules.placeOrder),
                )
                ?.also { used += it.id; daySpent += cost(it) }

            val dinner = if (includeDinner) pickRestaurant() else null

            val entries = listOfNotNull(morning, lunch, afternoon, dinner).map {
                ScheduleEntry(
                    id = idFactory(),
                    placeId = it.id,
                    type = it.type,
                    durationMin = it.durationMin,
                    priceSar = it.priceSar,
                )
            }
            val result = scheduleDay(day, entries)
            for (skipped in result.didNotFit) {
                // Not planned after all: refund it and let a later day use the place.
                daySpent -= skipped.priceSar * travelers
                used -= skipped.placeId
                usedRestaurants -= skipped.placeId
            }
            items += result.items
            spent += daySpent
        }

        return GenerationResult.Success(items, calculateTripCost(destination, days, travelers, items))
    }
}
