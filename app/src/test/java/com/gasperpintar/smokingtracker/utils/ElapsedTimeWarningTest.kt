package com.gasperpintar.smokingtracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class ElapsedTimeWarningTest {

    @Test
    fun warningIsShownBelowThresholdAndClearedWhenReached() {
        assertTrue(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(59), 60))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(60), 60))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofHours(2), 60))
    }

    @Test
    fun zeroDisablesWarning() {
        assertTrue(ElapsedTimeWarning.isValidMinutes(0))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ZERO, 0))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(1), 0))
    }

    @Test
    fun boundariesAndInvalidValuesAreHandled() {
        assertTrue(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(10_079), 10_080))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(10_080), 10_080))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(60), -1))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofMinutes(60), 10_081))
        assertFalse(ElapsedTimeWarning.shouldHighlight(Duration.ofSeconds(-1), 60))
    }
}