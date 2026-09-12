package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class JsonHelperTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var jsonHelper: JsonHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = TestProvider.getInMemoryDatabase(context)
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        jsonHelper = JsonHelper(achievementRepository = achievementRepository)
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun initializeAchievementsInsertsAchievementsIntoEmptyDatabase() = runBlocking {
        assertTrue(achievementRepository.getAll().isEmpty())

        jsonHelper.initializeAchievements(context)
        val achievements = achievementRepository.getAll()

        assertTrue(achievements.isNotEmpty())
        assertTrue(achievements.all { it.image.isNotBlank() })
        assertTrue(achievements.all { it.title.isNotBlank() })
        assertTrue(achievements.all { it.message.isNotBlank() })
    }

    @Test
    fun initializeAchievementsSetsCorrectUnitsForCategories() = runBlocking {
        jsonHelper.initializeAchievements(context)

        val achievements = achievementRepository.getAll().filter { it.category == AchievementCategory.CIGARETTES_AVOIDED }

        assertTrue(achievements.isNotEmpty())
        assertTrue(achievements.all { it.unit == AchievementUnit.CIGARETTES })
    }

    @Test
    fun initializeAchievementsDoesNotInsertDuplicates() = runBlocking {
        jsonHelper.initializeAchievements(context)

        val countBefore = achievementRepository.getAll().size
        jsonHelper.initializeAchievements(context)
        val countAfter = achievementRepository.getAll().size

        assertEquals(countBefore, countAfter)
    }
}