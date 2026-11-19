package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class AchievementTest {

    private lateinit var achievements: Map<Achievement.Category, List<AchievementEntry>>

    @Before
    fun setup() {
        val type = object : TypeToken<Map<Achievement.Category, List<AchievementEntry>>>() {}.type
        achievements = File("src/main/res/raw/achievements.json").bufferedReader().use { Gson().fromJson(it, type) }
    }

    @Test
    fun achievementEnumsHaveSameSize() {
        assertEquals(Achievement.Icon.entries.size, Achievement.Title.entries.size)
        assertEquals(Achievement.Icon.entries.size, Achievement.Message.entries.size)
    }

    @Test
    fun jsonOrderMatchesAchievementEnums() {
        val entries = achievements.values.flatten()

        assertEquals(Achievement.Icon.entries.map { it.name }, entries.map { it.icon })
        assertEquals(Achievement.Title.entries.map { it.name }, entries.map { it.title })
        assertEquals(Achievement.Message.entries.map { it.name }, entries.map { it.message })
    }

    @Test
    fun smokeFreeTimeEntriesHaveValidUnits() {
        achievements.getValue(Achievement.Category.SMOKE_FREE_TIME).forEach {
            Achievement.Unit.valueOf(value = it.unit)
        }
    }

    @Test
    fun toSecondsConvertsTimeUnits() {
        assertEquals(7_200L, Achievement.Unit.HOURS.toSeconds(value = 2))
        assertEquals(172_800L, Achievement.Unit.DAYS.toSeconds(value = 2))
        assertEquals(1_209_600L, Achievement.Unit.WEEKS.toSeconds(value = 2))
        assertEquals(5_184_000L, Achievement.Unit.MONTHS.toSeconds(value = 2))
        assertEquals(63_072_000L, Achievement.Unit.YEARS.toSeconds(value = 2))
    }

    @Test
    fun toSecondsReturnsNullForCigarettes() {
        assertNull(Achievement.Unit.CIGARETTES.toSeconds(value = 20))
    }
}