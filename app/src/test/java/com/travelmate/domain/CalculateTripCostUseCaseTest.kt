package com.travelmate.domain

import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.fakes.SampleData
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTripCostUseCaseTest {

    private val calculate = CalculateTripCostUseCase()
    private val destination = SampleData.destination(accommodationPerNightSar = 100, transportPerDaySar = 10)

    private fun item(type: PlaceType, priceSar: Int, day: Int = 1) =
        ItineraryItem(id = "i-$type-$priceSar", placeId = "p-$type-$priceSar", type = type, day = day, order = 0, startMin = 540, endMin = 600, priceSar = priceSar)

    @Test
    fun rooms_roundUp_threeTravelersNeedTwoRooms() {
        assertEquals(1, calculate.rooms(1))
        assertEquals(1, calculate.rooms(2))
        assertEquals(2, calculate.rooms(3))
        assertEquals(5, calculate.rooms(10))
        // 3 days = 2 nights, 3 travelers = 2 rooms: 100 x 2 x 2
        assertEquals(400, calculate.accommodation(destination, days = 3, travelers = 3))
    }

    @Test
    fun oneDayTrip_hasNoNights() {
        assertEquals(0, calculate.nights(1))
        val cost = calculate(destination, days = 1, travelers = 2, items = emptyList())
        assertEquals(0, cost.accommodationSar)
        assertEquals(10, cost.transportSar)
        assertEquals(10, cost.totalSar)
    }

    @Test
    fun activitiesAndFood_areSplitAndMultipliedByTravelers() {
        val items = listOf(
            item(PlaceType.ATTRACTION, 10),
            item(PlaceType.ACTIVITY, 20),
            item(PlaceType.EVENT, 30),
            item(PlaceType.RESTAURANT, 40),
        )
        val cost = calculate(destination, days = 2, travelers = 2, items = items)
        assertEquals(100, cost.accommodationSar) // 1 night x 1 room
        assertEquals(20, cost.transportSar)      // 2 days
        assertEquals(120, cost.activitiesSar)    // (10 + 20 + 30) x 2
        assertEquals(80, cost.foodSar)           // 40 x 2
        assertEquals(320, cost.totalSar)
        assertEquals(-20, cost.remainingSar(300))
    }

    @Test
    fun fixedCost_isStayPlusTransport() {
        assertEquals(100 * 2 * 1 + 10 * 3, calculate.fixedCost(destination, days = 3, travelers = 2))
    }

    @Test
    fun dayCost_isTransportPlusThatDaysItems() {
        val dayItems = listOf(item(PlaceType.ATTRACTION, 10), item(PlaceType.RESTAURANT, 20))
        assertEquals(10 + (10 + 20) * 3, calculate.dayCost(dayItems, travelers = 3, destination = destination))
    }
}
