package com.gasperpintar.smokingtracker.type

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AchievementUnitTest {

    @Test
    fun toSecondsConvertsTimeUnits() {
        assertEquals(7_200L, AchievementUnit.HOURS.toSeconds(value = 2))
        assertEquals(172_800L, AchievementUnit.DAYS.toSeconds(value = 2))
        assertEquals(1_209_600L, AchievementUnit.WEEKS.toSeconds(value = 2))
        assertEquals(5_184_000L, AchievementUnit.MONTHS.toSeconds(value = 2))
        assertEquals(63_072_000L, AchievementUnit.YEARS.toSeconds(value = 2))
    }

    @Test
    fun toSecondsReturnsNullForCigarettes() {
        assertNull(AchievementUnit.CIGARETTES.toSeconds(value = 20))
    }
}
