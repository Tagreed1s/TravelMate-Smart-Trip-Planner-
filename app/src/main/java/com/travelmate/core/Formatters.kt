package com.travelmate.core

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatting for money, dates and times. All output is English with SAR as the currency. */
object Formatters {
    private val shortDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.US)
    private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM", Locale.US)
    private val weekdayDayFormat = DateTimeFormatter.ofPattern("EEE d", Locale.US)
    private val weekdayDayMonthFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.US)

    /** `money(2800)` -> "SAR 2,800". */
    fun money(sar: Int): String = "SAR " + NumberFormat.getIntegerInstance(Locale.US).format(sar)

    /** "Thu, 12 Nov". */
    fun shortDate(date: LocalDate): String = date.format(shortDateFormat)

    /** "12 - 16 Nov" in the same month, otherwise "28 Nov - 2 Dec". */
    fun dateRange(start: LocalDate, end: LocalDate): String =
        if (start.month == end.month && start.year == end.year) {
            "${start.dayOfMonth} - ${end.format(dayMonthFormat)}"
        } else {
            "${start.format(dayMonthFormat)} - ${end.format(dayMonthFormat)}"
        }

    /** Minutes after midnight to "09:00". */
    fun time(minutes: Int): String = "%02d:%02d".format(Locale.US, minutes / 60, minutes % 60)

    /** Visit length: 45 -> "45 min", 60 -> "1 h", 90 -> "1 h 30 min". */
    fun duration(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m min"
            m == 0 -> "$h h"
            else -> "$h h $m min"
        }
    }

    /** Day pill label: "Thu 12". */
    fun weekdayDay(date: LocalDate): String = date.format(weekdayDayFormat)

    /** Day header: "Fri 13 Nov". */
    fun weekdayDayMonth(date: LocalDate): String = date.format(weekdayDayMonthFormat)

    /** Per-person price: 0 -> "Free", otherwise like [money]. */
    fun priceOrFree(sar: Int): String = if (sar == 0) "Free" else money(sar)

    /** Rating with one decimal: 4.55 -> "4.6". */
    fun rating(value: Double): String = "%.1f".format(Locale.US, value)

    /** Review counts: 950 -> "950", 2140 -> "2.1K". */
    fun compactCount(count: Int): String = when {
        count < 1_000 -> count.toString()
        count < 1_000_000 -> "%.1fK".format(Locale.US, count / 1_000.0).replace(".0K", "K")
        else -> "%.1fM".format(Locale.US, count / 1_000_000.0).replace(".0M", "M")
    }
}
