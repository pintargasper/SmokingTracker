package com.gasperpintar.smokingtracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class ElapsedTimeSettingsTest {

    @Test
    fun warningStartsExactlyAtConfiguredThreshold() {
        val threshold = ElapsedTimeSettings.DEFAULT_WARNING_MINUTES

        assertFalse(ElapsedTimeSettings.isWarningElapsed(Duration.ofMinutes(59), threshold))
        assertTrue(ElapsedTimeSettings.isWarningElapsed(Duration.ofMinutes(60), threshold))
        assertTrue(ElapsedTimeSettings.isWarningElapsed(Duration.ofHours(2), threshold))
    }

    @Test
    fun warningSupportsBoundariesAndRejectsInvalidOrNegativeValues() {
        assertTrue(ElapsedTimeSettings.isWarningElapsed(Duration.ofMinutes(1), 1))
        assertTrue(ElapsedTimeSettings.isWarningElapsed(Duration.ofMinutes(10_080), 10_080))
        assertFalse(ElapsedTimeSettings.isWarningElapsed(Duration.ofMinutes(60), 0))
        assertFalse(ElapsedTimeSettings.isWarningElapsed(Duration.ofMinutes(60), 10_081))
        assertFalse(ElapsedTimeSettings.isWarningElapsed(Duration.ofSeconds(-1), 60))
    }
}
