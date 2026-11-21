package com.travelmate.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormattersTest {

    @Test
    fun money_usesSarAndThousandsSeparator() {
        assertEquals("SAR 2,800", Formatters.money(2800))
        assertEquals("SAR 0", Formatters.money(0))
        assertEquals("SAR 1,000,000", Formatters.money(1_000_000))
    }

    @Test
    fun priceOrFree_showsFreeForZero() {
        assertEquals("Free", Formatters.priceOrFree(0))
        assertEquals("SAR 10", Formatters.priceOrFree(10))
    }

    @Test
    fun shortDate_showsWeekdayDayAndMonth() {
        assertEquals("Thu, 12 Nov", Formatters.shortDate(LocalDate.of(2026, 11, 12)))
    }

    @Test
    fun dateRange_sameMonth_showsMonthOnce() {
        assertEquals("12 - 16 Nov", Formatters.dateRange(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 16)))
    }

    @Test
    fun dateRange_differentMonths_showsBothMonths() {
        assertEquals("28 Nov - 2 Dec", Formatters.dateRange(LocalDate.of(2026, 11, 28), LocalDate.of(2026, 12, 2)))
    }

    @Test
    fun time_padsHoursAndMinutes() {
        assertEquals("09:00", Formatters.time(540))
        assertEquals("13:05", Formatters.time(785))
        assertEquals("22:00", Formatters.time(1320))
    }

    @Test
    fun duration_usesHoursAndMinutes() {
        assertEquals("45 min", Formatters.duration(45))
        assertEquals("1 h", Formatters.duration(60))
        assertEquals("1 h 30 min", Formatters.duration(90))
    }

    @Test
    fun compactCount_shortensThousands() {
        assertEquals("950", Formatters.compactCount(950))
        assertEquals("2.1K", Formatters.compactCount(2140))
        assertEquals("3K", Formatters.compactCount(3000))
    }

    @Test
    fun weekdayLabels() {
        assertEquals("Thu 12", Formatters.weekdayDay(LocalDate.of(2026, 11, 12)))
        assertEquals("Fri 13 Nov", Formatters.weekdayDayMonth(LocalDate.of(2026, 11, 13)))
    }
}
