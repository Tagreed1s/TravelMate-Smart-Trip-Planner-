package com.travelmate.domain.usecase

import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.PlaceType

/** One place to put on a day, in the order it should happen. */
data class ScheduleEntry(
    val id: String,
    val placeId: String,
    val type: PlaceType,
    val durationMin: Int,
    /** Per person. */
    val priceSar: Int,
)

data class ScheduleResult(
    val items: List<ItineraryItem>,
    val didNotFit: List<ScheduleEntry>,
)

/**
 * Gives each entry of one day a start and end time (plan Section 9.4):
 * the day starts at 09:00, items are separated by a 60 minute gap,
 * and an entry that would end after 22:00 is skipped and reported in [ScheduleResult.didNotFit].
 * Re-run after every remove so times always stay consistent.
 */
class ScheduleDayUseCase {

    operator fun invoke(day: Int, entries: List<ScheduleEntry>): ScheduleResult {
        val items = mutableListOf<ItineraryItem>()
        val didNotFit = mutableListOf<ScheduleEntry>()
        var cursor = PlanningRules.DAY_START_MIN
        for (entry in entries) {
            val end = cursor + entry.durationMin
            if (end > PlanningRules.DAY_END_MIN) {
                didNotFit += entry
                continue
            }
            items += ItineraryItem(
                id = entry.id,
                placeId = entry.placeId,
                type = entry.type,
                day = day,
                order = items.size,
                startMin = cursor,
                endMin = end,
                priceSar = entry.priceSar,
            )
            cursor = end + PlanningRules.GAP_MIN
        }
        return ScheduleResult(items, didNotFit)
    }
}
