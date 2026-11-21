package com.travelmate.domain

import com.travelmate.domain.usecase.CalculateTripCostUseCase
import com.travelmate.domain.usecase.SuggestBudgetUseCase
import com.travelmate.fakes.SampleData
import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestBudgetUseCaseTest {

    private val suggest = SuggestBudgetUseCase(CalculateTripCostUseCase())

    @Test
    fun roundsUpToNearestHundred() {
        // fixed = 100 x 2 nights x 1 room + 10 x 3 days = 230; daily = 3 x 2 x avg(10, 30) = 120; 350 -> 400
        val destination = SampleData.destination(accommodationPerNightSar = 100, transportPerDaySar = 10, dailySpendMinSar = 10, dailySpendMaxSar = 30)
        assertEquals(400, suggest(destination, days = 3, travelers = 2))
    }

    @Test
    fun exactHundred_staysTheSame() {
        // 1 day: no nights; transport 80 + 1 x 1 x 20 = 100
        val destination = SampleData.destination(transportPerDaySar = 80, dailySpendMinSar = 10, dailySpendMaxSar = 30)
        assertEquals(100, suggest(destination, days = 1, travelers = 1))
    }
}
