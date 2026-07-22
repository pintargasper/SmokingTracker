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
    fun loadAchievementsFromJsonReturnsSmokeFreeAchievements() {
        val result = jsonHelper.loadAchievementsFromJson(
            context,
            AchievementCategory.SMOKE_FREE_TIME
        )

        assertTrue(result.isNotEmpty())

        result.forEach {
            assertEquals(AchievementCategory.SMOKE_FREE_TIME, it.category)
        }

        assertTrue(result.all { it.image.isNotBlank() })
        assertTrue(result.all { it.title.isNotBlank() })
        assertTrue(result.all { it.message.isNotBlank() })
    }

    @Test
    fun loadAchievementsFromJsonReturnsCigarettesAvoidedAchievements() {
        val result = jsonHelper.loadAchievementsFromJson(
            context,
            AchievementCategory.CIGARETTES_AVOIDED
        )

        assertTrue(result.isNotEmpty())

        result.forEach {
            assertEquals(AchievementCategory.CIGARETTES_AVOIDED, it.category)
            assertEquals(AchievementUnit.CIGARETTES, it.unit)
        }

        assertTrue(result.all { it.image.isNotBlank() })
        assertTrue(result.all { it.title.isNotBlank() })
        assertTrue(result.all { it.message.isNotBlank() })
    }

    @Test
    fun initializeAchievementsIfNeededInsertsAchievementsIntoEmptyDatabase() = runBlocking {
        assertTrue(achievementRepository.getAll().isEmpty())

        jsonHelper.initializeAchievementsIfNeeded(context)
        val achievements = achievementRepository.getAll()

        assertTrue(achievements.isNotEmpty())
    }

    @Test
    fun initializeAchievementsIfNeededDoesNotInsertDuplicates() = runBlocking {
        jsonHelper.initializeAchievementsIfNeeded(context)

        val countBefore = achievementRepository.getAll().size
        jsonHelper.initializeAchievementsIfNeeded(context)
        val countAfter = achievementRepository.getAll().size

        assertEquals(countBefore, countAfter)
    }

    @Test
    fun migratePreservesIdsAndCount() = runBlocking {
        jsonHelper.initializeAchievementsIfNeeded(context)

        val before = achievementRepository.getAll()
        jsonHelper.migrate(achievementRepository)
        val after = achievementRepository.getAll()

        assertEquals(before.size, after.size)
        assertEquals(before.map { it.id }, after.map { it.id })
    }
}