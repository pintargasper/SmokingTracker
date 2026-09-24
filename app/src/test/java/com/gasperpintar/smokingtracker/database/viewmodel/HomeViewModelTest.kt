package com.gasperpintar.smokingtracker.database.viewmodel

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.fake.FakeAchievementDao
import com.gasperpintar.smokingtracker.database.fake.FakeHistoryDao
import com.gasperpintar.smokingtracker.database.fake.FakeSettingsDao
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class HomeViewModelTest {

    private val now = LocalDateTime.now()
    private lateinit var historyDao: FakeHistoryDao
    private lateinit var achievementDao: FakeAchievementDao
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() = runBlocking {
        historyDao = FakeHistoryDao()
        achievementDao = FakeAchievementDao()
        val settingsDao = FakeSettingsDao()
        settingsDao.insert(entity = SettingsEntity.default())

        viewModel = HomeViewModel(
            achievementRepository = AchievementRepository(achievementDao = achievementDao),
            historyRepository = HistoryRepository(historyDao = historyDao),
            settingsRepository = SettingsRepository(settingsDao = settingsDao)
        )
    }

    @Test
    fun insertSmokedEntryResetsAchievements() = runBlocking {
        viewModel.insert(isLent = false)

        assertEquals(0, historyDao.items.single().lent)
        assertEquals(listOf(true), achievementDao.resetAllCalls)
    }

    @Test
    fun insertLentEntryDoesNotResetAchievements() = runBlocking {
        viewModel.insert(isLent = true)

        assertEquals(1, historyDao.items.single().lent)
        assertTrue(achievementDao.resetAllCalls.isEmpty())
    }

    @Test
    fun deleteLastSmokedEntryRestoresAchievements() = runBlocking {
        val (_, last, _) = insertHistory()
        viewModel.getState()

        viewModel.delete(entry = HistoryEntry.fromEntity(entity = last))

        assertEquals(listOf(false), achievementDao.resetAllCalls)
        assertTrue(historyDao.items.none { it.id == last.id })
    }

    @Test
    fun deleteOtherEntriesDoesNotRestoreAchievements() = runBlocking {
        val (first, _, lent) = insertHistory()
        viewModel.getState()

        viewModel.delete(entry = HistoryEntry.fromEntity(entity = first))
        viewModel.delete(entry = HistoryEntry.fromEntity(entity = lent))

        assertTrue(achievementDao.resetAllCalls.isEmpty())
    }

    @Test
    fun updateLastSmokedEntryRestoresAchievements() = runBlocking {
        val (_, last, _) = insertHistory()
        val dateTime = now.minusHours(5)
        viewModel.getState()

        viewModel.update(entry = HistoryEntry.fromEntity(entity = last), dateTime = dateTime, isLent = true)

        assertEquals(listOf(false), achievementDao.resetAllCalls)
        assertEquals(last.copy(createdAt = dateTime, lent = 1), historyDao.items.single { it.id == last.id })
    }

    @Test
    fun getStateReturnsEntriesOfSelectedDay() = runBlocking {
        val today = TimeHelper.dayDate(dayEndMinutes = 0)
        listOf(
            HistoryEntity(id = 0, lent = 0, createdAt = today.atTime(12, 0)),
            HistoryEntity(id = 0, lent = 1, createdAt = today.atTime(13, 0)),
            HistoryEntity(id = 0, lent = 0, createdAt = today.minusDays(1).atTime(12, 0))
        ).forEach { historyDao.insert(entity = it) }

        val state = viewModel.getState()

        assertEquals(today, state.selectedDate)
        assertEquals(2, state.dailyCount)
        assertEquals(listOf(true, false), state.history.map { it.isLent })

        viewModel.previousDay()
        val previousState = viewModel.getState()

        assertEquals(today.minusDays(1), previousState.selectedDate)
        assertEquals(1, previousState.dailyCount)
    }

    private suspend fun insertHistory(): Triple<HistoryEntity, HistoryEntity, HistoryEntity> {
        listOf(
            HistoryEntity(id = 0, lent = 0, createdAt = now.minusHours(2)),
            HistoryEntity(id = 0, lent = 0, createdAt = now.minusHours(1)),
            HistoryEntity(id = 0, lent = 1, createdAt = now.minusMinutes(30))
        ).forEach { historyDao.insert(entity = it) }

        val (first, last, lent) = historyDao.items
        return Triple(first, last, lent)
    }
}