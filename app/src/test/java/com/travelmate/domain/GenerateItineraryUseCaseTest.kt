package com.travelmate.domain

import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.TripRequest
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.domain.usecase.GenerateItineraryUseCase
import com.travelmate.domain.usecase.GenerationResult
import com.travelmate.domain.usecase.PlanningRules
import com.travelmate.domain.usecase.ScheduleDayUseCase
import com.travelmate.fakes.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GenerateItineraryUseCaseTest {

    private val calculateCost = CalculateTripCostUseCase()
    private val generate = GenerateItineraryUseCase(ScheduleDayUseCase(), calculateCost)

    // accommodation 100 per night, transport 10 per day
    private val destination = SampleData.destinationA
    private val places = SampleData.places(destination.id)

    private fun request(
        days: Int = 3,
        travelers: Int = 2,
        budgetSar: Int = 1_000,
        interests: Set<PlaceType> = setOf(PlaceType.ATTRACTION, PlaceType.ACTIVITY),
    ) = TripRequest(destination.id, LocalDate.of(2026, 11, 12), days, travelers, budgetSar, interests)

    private fun run(request: TripRequest, placeList: List<Place> = places): GenerationResult {
        var counter = 0
        return generate(destination, placeList, request) { "item-${counter++}" }
    }

    private fun success(request: TripRequest, placeList: List<Place> = places): GenerationResult.Success {
        val result = run(request, placeList)
        assertTrue("Expected Success but was $result", result is GenerationResult.Success)
        return result as GenerationResult.Success
    }

    private fun GenerationResult.Success.day(day: Int): List<ItineraryItem> =
        items.filter { it.day == day }.sortedBy { it.order }

    @Test
    fun success_staysWithinBudget_andPicksNearestAfternoonPlace() {
        val result = success(request())
        assertTrue(result.cost.totalSar <= 1_000)
        // Morning = best rated (attr-1), lunch = best restaurant, afternoon = nearest to the morning place (act-1).
        assertEquals(listOf("sample-a-attr-1", "sample-a-food-1", "sample-a-act-1"), result.day(1).map { it.placeId })
        assertEquals(calculateCost(destination, 3, 2, result.items), result.cost)
    }

    @Test
    fun budgetBelowStayAndTransport_returnsMinimum() {
        // 2 nights x 1 room x 100 + 3 days x 10 = 230
        assertEquals(GenerationResult.BudgetTooLow(minimumSar = 230), run(request(budgetSar = 229)))
    }

    @Test
    fun budgetExactlyStayAndTransport_givesEmptyDays() {
        val result = success(request(budgetSar = 230))
        assertTrue(result.items.isEmpty())
        assertEquals(230, result.cost.totalSar)
    }

    @Test
    fun lunchIsPlanned_withoutRestaurantsInterest_butNoDinner() {
        val result = success(request(days = 2, budgetSar = 5_000))
        for (day in 1..2) {
            assertEquals(1, result.day(day).count { it.type == PlaceType.RESTAURANT })
        }
    }

    @Test
    fun dinnerIsPlanned_onlyWithRestaurantsInterest() {
        val result = success(request(days = 1, travelers = 1, budgetSar = 5_000, interests = setOf(PlaceType.ATTRACTION, PlaceType.RESTAURANT)))
        val day = result.day(1)
        assertEquals(2, day.count { it.type == PlaceType.RESTAURANT })
        // Order: morning, lunch, afternoon, dinner
        assertEquals(listOf(PlaceType.ATTRACTION, PlaceType.RESTAURANT, PlaceType.ATTRACTION, PlaceType.RESTAURANT), day.map { it.type })
    }

    @Test
    fun restaurantsRepeat_onlyAfterAllWereUsed() {
        val threeRestaurants = listOf(
            SampleData.place("r1", PlaceType.RESTAURANT, rating = 5.0),
            SampleData.place("r2", PlaceType.RESTAURANT, rating = 4.0),
            SampleData.place("r3", PlaceType.RESTAURANT, rating = 3.0),
        )
        val result = success(request(days = 2, travelers = 1, budgetSar = 5_000, interests = setOf(PlaceType.RESTAURANT)), threeRestaurants)
        val meals = (1..2).flatMap { result.day(it) }.map { it.placeId }
        assertEquals(listOf("r1", "r2", "r3", "r1"), meals)
        // Never the same restaurant twice in one day.
        for (day in 1..2) {
            val ids = result.day(day).map { it.placeId }
            assertEquals(ids.distinct(), ids)
        }
    }

    @Test
    fun onlyMeals_whenNoNonFoodPlaceMatchesInterests() {
        val noEvents = places.filter { it.type != PlaceType.EVENT }
        val result = success(request(days = 2, budgetSar = 5_000, interests = setOf(PlaceType.EVENT)), noEvents)
        assertTrue(result.items.isNotEmpty())
        assertTrue(result.items.all { it.type == PlaceType.RESTAURANT })
        for (day in 1..2) assertEquals(1, result.day(day).size)
    }

    @Test
    fun output_isDeterministic() {
        val request = request(days = 4, budgetSar = 2_000, interests = PlaceType.entries.toSet())
        assertEquals(run(request), run(request))
    }

    @Test
    fun neverExceedsBudget() {
        for (budget in 230..900 step 7) {
            val result = run(request(budgetSar = budget, interests = PlaceType.entries.toSet()))
            assertTrue(result is GenerationResult.Success)
            assertTrue("budget $budget", (result as GenerationResult.Success).cost.totalSar <= budget)
        }
    }

    @Test
    fun scheduledTimes_followThePlanningRules() {
        val result = success(request(days = 2, budgetSar = 5_000, interests = PlaceType.entries.toSet()))
        for (day in 1..2) {
            val items = result.day(day)
            assertEquals(PlanningRules.DAY_START_MIN, items.first().startMin)
            assertTrue(items.all { it.endMin <= PlanningRules.DAY_END_MIN })
            items.zipWithNext().forEach { (a, b) -> assertEquals(a.endMin + PlanningRules.GAP_MIN, b.startMin) }
        }
    }
}
