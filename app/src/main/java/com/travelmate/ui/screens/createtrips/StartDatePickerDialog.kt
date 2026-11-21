package com.travelmate.ui.screens.createtrips

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Material 3 date picker with past dates disabled. The picker works in UTC milliseconds. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val selectableDates = remember(today) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !utcTimeMillis.toLocalDate().isBefore(today)
            override fun isSelectableYear(year: Int) = year >= today.year
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let { onConfirm(it.toLocalDate()) } ?: onDismiss() },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = pickerState)
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
