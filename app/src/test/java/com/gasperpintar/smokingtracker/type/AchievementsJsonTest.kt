package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.database.model.AchievementJsonEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class AchievementsJsonTest {

    private lateinit var achievements: Map<AchievementCategory, List<AchievementJsonEntry>>

    @Before
    fun setup() {
        val type = object : TypeToken<Map<AchievementCategory, List<AchievementJsonEntry>>>() {}.type
        achievements = File("src/main/res/raw/achievements.json").bufferedReader().use { Gson().fromJson(it, type) }
    }

    @Test
    fun achievementEnumsHaveSameSize() {
        assertEquals(AchievementIcon.entries.size, AchievementTitle.entries.size)
        assertEquals(AchievementIcon.entries.size, AchievementMessage.entries.size)
    }

    @Test
    fun jsonOrderMatchesAchievementEnums() {
        val entries = achievements.values.flatten()

        assertEquals(AchievementIcon.entries.map { it.name }, entries.map { it.icon })
        assertEquals(AchievementTitle.entries.map { it.name }, entries.map { it.title })
        assertEquals(AchievementMessage.entries.map { it.name }, entries.map { it.message })
    }

    @Test
    fun smokeFreeTimeEntriesHaveValidUnits() {
        achievements.getValue(AchievementCategory.SMOKE_FREE_TIME).forEach {
            AchievementUnit.valueOf(value = it.unit)
        }
    }
}