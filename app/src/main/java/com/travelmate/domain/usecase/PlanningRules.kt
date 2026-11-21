package com.travelmate.domain.usecase

import com.travelmate.domain.model.Place

object PlanningRules {
    const val DAY_START_MIN = 9 * 60       // 09:00
    const val DAY_END_MIN = 22 * 60        // nothing may end after 22:00
    const val GAP_MIN = 60                 // gap between items
    const val MIN_DAYS = 1
    const val MAX_DAYS = 14
    const val MIN_TRAVELERS = 1
    const val MAX_TRAVELERS = 10
    const val MAX_BUDGET_SAR = 1_000_000
    const val TRAVELERS_PER_ROOM = 2

    /** Tie-break used everywhere: rating descending, then priceSar ascending, then id ascending. */
    val placeOrder: Comparator<Place> =
        compareByDescending<Place> { it.rating }.thenBy { it.priceSar }.thenBy { it.id }
}
