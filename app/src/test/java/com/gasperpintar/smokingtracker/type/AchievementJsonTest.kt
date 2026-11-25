package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.model.AchievementJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class AchievementJsonTest {

    private lateinit var achievementJsons: Map<AchievementJson.Category, List<AchievementJson>>

    @Before
    fun setup() {
        val type = object : TypeToken<Map<AchievementJson.Category, List<AchievementJson>>>() {}.type
        achievementJsons = File("src/main/res/raw/achievements.json").bufferedReader().use { Gson().fromJson(it, type) }
    }

    @Test
    fun achievementJsonEnumsHaveSameSize() {
        assertEquals(AchievementJson.Icon.entries.size, AchievementJson.Title.entries.size)
        assertEquals(AchievementJson.Icon.entries.size, AchievementJson.Message.entries.size)
    }

    @Test
    fun jsonOrderMatchesAchievementJsonEnums() {
        val entries = achievementJsons.values.flatten()

        assertEquals(AchievementJson.Icon.entries.map { it.name }, entries.map { it.icon })
        assertEquals(AchievementJson.Title.entries.map { it.name }, entries.map { it.title })
        assertEquals(AchievementJson.Message.entries.map { it.name }, entries.map { it.message })
    }

    @Test
    fun smokeFreeTimeEntriesHaveValidUnits() {
        achievementJsons.getValue(key = AchievementJson.Category.SMOKE_FREE_TIME).forEach {
            AchievementJson.Unit.valueOf(value = it.unit)
        }
    }

    @Test
    fun toSecondsConvertsTimeUnits() {
        assertEquals(7_200L, AchievementJson.Unit.HOURS.toSeconds(value = 2))
        assertEquals(172_800L, AchievementJson.Unit.DAYS.toSeconds(value = 2))
        assertEquals(1_209_600L, AchievementJson.Unit.WEEKS.toSeconds(value = 2))
        assertEquals(5_184_000L, AchievementJson.Unit.MONTHS.toSeconds(value = 2))
        assertEquals(63_072_000L, AchievementJson.Unit.YEARS.toSeconds(value = 2))
    }

    @Test
    fun toSecondsReturnsNullForCigarettes() {
        assertNull(AchievementJson.Unit.CIGARETTES.toSeconds(value = 20))
    }
}