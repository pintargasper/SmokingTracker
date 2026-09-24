package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(value = AndroidJUnit4::class)
class AchievementEvaluatorTest {

    private val lastSmokeTime = LocalDateTime.of(2026, 1, 1, 10, 0)

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var evaluator: AchievementEvaluator

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        database = TestProvider.getInMemoryDatabase(context)
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        historyRepository = HistoryRepository(historyDao = database.historyDao())

        val notificationsSettingsRepository = NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())
        notificationsSettingsRepository.insert(settings = NotificationsSettingsEntity.default().copy(achievements = false))

        evaluator = AchievementEvaluator(
            context = context,
            historyRepository = historyRepository,
            achievementRepository = achievementRepository,
            notificationsSettingsRepository = notificationsSettingsRepository
        )
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun evaluateUnlocksSmokeFreeAchievementAfterRequiredTime() = runBlocking {
        insertAchievement(value = 1, unit = AchievementUnit.DAYS)

        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(25))

        val achievement = achievementRepository.getAll().single()
        assertEquals(1L, achievement.times)
        assertEquals(lastSmokeTime.plusDays(1), achievement.lastAchieved)
        assertEquals(false, achievement.reset)
        assertEquals(true, achievement.notify)
    }

    @Test
    fun evaluateDoesNotUnlockBeforeRequiredTime() = runBlocking {
        insertAchievement(value = 1, unit = AchievementUnit.DAYS)

        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(23))

        val achievement = achievementRepository.getAll().single()
        assertEquals(0L, achievement.times)
        assertEquals(null, achievement.lastAchieved)
        assertEquals(true, achievement.reset)
    }

    @Test
    fun evaluateIsIdempotentForSameLastSmoke() = runBlocking {
        insertAchievement(value = 1, unit = AchievementUnit.DAYS)

        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(25))
        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(30))

        assertEquals(1L, achievementRepository.getAll().single().times)
    }

    @Test
    fun evaluateUnlocksAgainAfterNewCigarette() = runBlocking {
        insertAchievement(value = 1, unit = AchievementUnit.DAYS)
        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(25))

        val nextSmokeTime = lastSmokeTime.plusDays(2)
        achievementRepository.resetAll(state = true)
        evaluator.evaluate(lastSmokeTime = nextSmokeTime, now = nextSmokeTime.plusHours(25))

        val achievement = achievementRepository.getAll().single()
        assertEquals(2L, achievement.times)
        assertEquals(nextSmokeTime.plusDays(1), achievement.lastAchieved)
    }

    @Test
    fun evaluateUnlocksCigarettesAvoidedUsingDailyAverage() = runBlocking {
        insertAchievement(value = 20, unit = AchievementUnit.CIGARETTES, category = AchievementCategory.CIGARETTES_AVOIDED)
        listOf(lastSmokeTime.minusDays(2), lastSmokeTime.minusDays(1)).forEach { day ->
            repeat(times = 10) { historyRepository.insert(entry = HistoryEntity(id = 0, lent = 0, createdAt = day.plusMinutes(it.toLong()))) }
        }

        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(47))
        assertEquals(0L, achievementRepository.getAll().single().times)

        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusHours(49))
        val achievement = achievementRepository.getAll().single()
        assertEquals(1L, achievement.times)
        assertEquals(lastSmokeTime.plusDays(2), achievement.lastAchieved)
    }

    @Test
    fun evaluateSkipsCigarettesAvoidedWithoutHistory() = runBlocking {
        insertAchievement(value = 20, unit = AchievementUnit.CIGARETTES, category = AchievementCategory.CIGARETTES_AVOIDED)

        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusYears(1))

        assertEquals(0L, achievementRepository.getAll().single().times)
    }

    @Test
    fun evaluateWithoutAchievementsDoesNothing() = runBlocking {
        evaluator.evaluate(lastSmokeTime = lastSmokeTime, now = lastSmokeTime.plusYears(1))

        assertTrue(achievementRepository.getAll().isEmpty())
    }

    private suspend fun insertAchievement(
        value: Int,
        unit: AchievementUnit,
        category: AchievementCategory = AchievementCategory.SMOKE_FREE_TIME
    ) {
        achievementRepository.insert(
            entries = listOf(
                AchievementEntity(
                    id = 0,
                    image = "W1D",
                    value = value,
                    title = "DAYS1",
                    message = "DAYS1",
                    times = 0,
                    lastAchieved = null,
                    reset = true,
                    notify = true,
                    category = category,
                    unit = unit
                )
            )
        )
    }
}