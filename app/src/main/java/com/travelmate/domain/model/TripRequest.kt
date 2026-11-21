package com.travelmate.domain.model

import java.time.LocalDate

/** What the user entered in the Plan Trip sheet. Nullable fields are checked by ValidateTripInputUseCase. */
data class TripRequest(
    val destinationId: String?,
    val startDate: LocalDate?,
    val days: Int,
    val travelers: Int,
    val budgetSar: Int?,
    val interests: Set<PlaceType>,
)
