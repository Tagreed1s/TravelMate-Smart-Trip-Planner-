package com.travelmate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.travelmate.domain.model.ExpenseCategory

/** Labels follow the Milestone 2 Trip Cost Summary mockup. */
val ExpenseCategory.label: String
    get() = when (this) {
        ExpenseCategory.FLIGHTS -> "Flights"
        ExpenseCategory.STAY -> "Accommodation"
        ExpenseCategory.TRANSPORT -> "Transportation"
        ExpenseCategory.FOOD -> "Food"
        ExpenseCategory.ACTIVITIES -> "Activities"
        ExpenseCategory.OTHER -> "Other"
    }

val ExpenseCategory.icon: ImageVector
    get() = when (this) {
        ExpenseCategory.FLIGHTS -> Icons.Rounded.Flight
        ExpenseCategory.STAY -> Icons.Rounded.Hotel
        ExpenseCategory.TRANSPORT -> Icons.Rounded.DirectionsCar
        ExpenseCategory.FOOD -> Icons.Rounded.Restaurant
        ExpenseCategory.ACTIVITIES -> Icons.Rounded.Hiking
        ExpenseCategory.OTHER -> Icons.Rounded.Payments
    }
