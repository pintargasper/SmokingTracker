package com.gasperpintar.smokingtracker.utils.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressFrequencyTest {

    @Test
    fun fromValueReturnsMatchingFrequency() {
        assertEquals(ProgressFrequency.HOURLY, ProgressFrequency.fromValue(value = 0))
        assertEquals(ProgressFrequency.DAILY, ProgressFrequency.fromValue(value = 1))
        assertEquals(ProgressFrequency.WEEKLY, ProgressFrequency.fromValue(value = 2))
    }

    @Test
    fun fromValueFallsBackToHourly() {
        assertEquals(ProgressFrequency.HOURLY, ProgressFrequency.fromValue(value = null))
        assertEquals(ProgressFrequency.HOURLY, ProgressFrequency.fromValue(value = 42))
    }
}
