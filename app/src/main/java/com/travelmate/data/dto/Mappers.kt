package com.travelmate.data.dto

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue
import com.travelmate.domain.model.Destination
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.domain.model.ItineraryItem
import com.travelmate.domain.model.Place
import com.travelmate.domain.model.PlaceType
import com.travelmate.domain.model.Trip
import java.time.LocalDate

private const val TAG = "Mappers"

/** Unknown values map to ATTRACTION and are logged (plan Section 7). */
fun placeTypeOf(value: String): PlaceType =
    PlaceType.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
        ?: PlaceType.ATTRACTION.also { Log.w(TAG, "Unknown place type '$value', using ATTRACTION") }

/** Reads one child into a DTO; a broken record is logged and skipped instead of breaking the whole list. */
inline fun <reified T : Any> DataSnapshot.readOrNull(): T? =
    runCatching { getValue(T::class.java) }
        .onFailure { Log.w("Mappers", "Skipping '$key': ${it.message}") }
        .getOrNull()

fun DestinationDto.toDomain(key: String) = Destination(
    id = id.ifBlank { key },
    name = name,
    country = country,
    summary = summary,
    description = description,
    heroImageUrl = heroImageUrl,
    imageUrls = imageUrls.filter { it.isNotBlank() },
    rating = rating,
    reviewCount = reviewCount,
    bestTimeToVisit = bestTimeToVisit,
    weatherRange = weatherRange,
    dailySpendMinSar = dailySpendMinSar,
    dailySpendMaxSar = dailySpendMaxSar,
    accommodationPerNightSar = accommodationPerNightSar,
    transportPerDaySar = transportPerDaySar,
    lat = lat,
    lng = lng,
    tips = tips.filter { it.isNotBlank() },
    sortOrder = sortOrder,
)

fun PlaceDto.toDomain(key: String, parentDestinationId: String) = Place(
    id = id.ifBlank { key },
    destinationId = destinationId.ifBlank { parentDestinationId },
    type = placeTypeOf(type),
    name = name,
    description = description,
    imageUrl = imageUrl,
    rating = rating,
    priceSar = priceSar,
    durationMin = durationMin,
    lat = lat,
    lng = lng,
    openingHours = openingHours,
    schedule = schedule?.takeIf { it.isNotBlank() },
)

fun ItemDto.toDomain(key: String) = ItineraryItem(
    id = id.ifBlank { key },
    placeId = placeId,
    type = placeTypeOf(type),
    day = day,
    order = order,
    startMin = startMin,
    endMin = endMin,
    priceSar = priceSar,
)

fun TripDto.toDomain(key: String) = Trip(
    id = id.ifBlank { key },
    destinationId = destinationId,
    destinationName = destinationName,
    heroImageUrl = heroImageUrl,
    name = name,
    startDate = runCatching { LocalDate.parse(startDate) }.getOrElse { LocalDate.now() },
    days = days,
    travelers = travelers,
    budgetSar = budgetSar,
    interests = interests.map(::placeTypeOf).toSet(),
    items = items.map { (itemKey, item) -> item.toDomain(itemKey) },
    totalCostSar = totalCostSar,
    placesCount = placesCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expenses = expenses.map { (expenseKey, expense) -> expense.toDomain(expenseKey) }.sortedBy { it.createdAt },
)

/** Unknown values map to OTHER. */
fun expenseCategoryOf(value: String): ExpenseCategory =
    ExpenseCategory.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: ExpenseCategory.OTHER

fun ExpenseDto.toDomain(key: String) = Expense(
    id = id.ifBlank { key },
    category = expenseCategoryOf(category),
    title = title,
    amountSar = amountSar,
    createdAt = createdAt,
)

/** One expense node; createdAt is set by the server on the first write. */
fun Expense.toWriteMap(): Map<String, Any> = mapOf(
    "id" to id,
    "category" to category.name,
    "title" to title,
    "amountSar" to amountSar,
    "createdAt" to if (createdAt > 0) createdAt else ServerValue.TIMESTAMP,
)

fun ItineraryItem.toWriteMap(): Map<String, Any> = mapOf(
    "id" to id,
    "placeId" to placeId,
    "type" to type.name,
    "day" to day,
    "order" to order,
    "startMin" to startMin,
    "endMin" to endMin,
    "priceSar" to priceSar,
)

fun List<ItineraryItem>.toWriteMap(): Map<String, Any> = associate { it.id to it.toWriteMap() }

fun List<ItineraryItem>.distinctPlaceCount(): Int = map { it.placeId }.distinct().size

/** The whole trip node for the first write; both timestamps are set by the server. */
fun Trip.toWriteMap(): Map<String, Any> = mapOf(
    "id" to id,
    "destinationId" to destinationId,
    "destinationName" to destinationName,
    "heroImageUrl" to heroImageUrl,
    "name" to name,
    "startDate" to startDate.toString(),
    "days" to days,
    "travelers" to travelers,
    "budgetSar" to budgetSar,
    "interests" to interests.map { it.name }.sorted(),
    "items" to items.toWriteMap(),
    "totalCostSar" to totalCostSar,
    "placesCount" to items.distinctPlaceCount(),
    "createdAt" to ServerValue.TIMESTAMP,
    "updatedAt" to ServerValue.TIMESTAMP,
)
