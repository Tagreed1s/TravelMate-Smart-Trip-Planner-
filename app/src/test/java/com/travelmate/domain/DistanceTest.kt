package com.travelmate.domain

import com.travelmate.domain.util.Distance
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceTest {

    @Test
    fun londonToParis_isAbout343Km() {
        val km = Distance.km(51.5074, -0.1278, 48.8566, 2.3522)
        val expected = 343.5
        assertEquals(expected, km, expected * 0.01)
    }

    @Test
    fun samePoint_isZero() {
        assertEquals(0.0, Distance.km(24.7, 46.7, 24.7, 46.7), 1e-9)
    }
}
