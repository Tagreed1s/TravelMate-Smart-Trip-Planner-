package com.travelmate.domain.usecase

import com.travelmate.domain.model.Destination

/**
 * Suggested budget (plan Section 9.6):
 * stay + transport + days x travelers x average daily spend, rounded up to the nearest 100.
 */
class SuggestBudgetUseCase(private val calculateTripCost: CalculateTripCostUseCase) {

    operator fun invoke(destination: Destination, days: Int, travelers: Int): Int {
        val avgDaily = (destination.dailySpendMinSar + destination.dailySpendMaxSar) / 2
        val suggested = calculateTripCost.fixedCost(destination, days, travelers) + days * travelers * avgDaily
        return roundUpToHundred(suggested)
    }

    private fun roundUpToHundred(value: Int): Int = ((value + 99) / 100) * 100
}
