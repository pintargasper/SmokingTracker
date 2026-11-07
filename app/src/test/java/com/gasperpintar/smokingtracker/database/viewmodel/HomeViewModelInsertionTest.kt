package com.gasperpintar.smokingtracker.database.viewmodel

import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.fake.FakeAchievementDao
import com.gasperpintar.smokingtracker.database.fake.FakeHistoryDao
import com.gasperpintar.smokingtracker.database.fake.FakeSettingsDao
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelInsertionTest {

    private lateinit var historyDao: FakeHistoryDao
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        historyDao = FakeHistoryDao()
        val settingsDao = FakeSettingsDao()
        settingsDao.insert(entity = SettingsEntity.default())
        viewModel = HomeViewModel(
            achievementRepository = AchievementRepository(achievementDao = FakeAchievementDao()),
            historyRepository = HistoryRepository(historyDao = historyDao),
            settingsRepository = SettingsRepository(settingsDao = settingsDao)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun insertNowIgnoresConcurrentRequests() = runTest {
        val insertionStarted = CompletableDeferred<Unit>()
        val continueInsertion = CompletableDeferred<Unit>()
        historyDao.insertHook = {
            insertionStarted.complete(Unit)
            continueInsertion.await()
        }
        val events = mutableListOf<Boolean>()
        val collector = launch { viewModel.addEvents.collect(events::add) }

        viewModel.insertNow()
        runCurrent()
        assertTrue(insertionStarted.isCompleted)
        assertTrue(viewModel.isAdding.value)

        viewModel.insertNow()
        runCurrent()
        continueInsertion.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, historyDao.items.size)
        assertEquals(0, historyDao.items.single().lent)
        assertEquals(listOf(true), events)
        assertFalse(viewModel.isAdding.value)
        collector.cancel()
    }

    @Test
    fun insertNowEmitsFailureWithoutInserting() = runTest {
        historyDao.insertFailure = IllegalStateException("Insert failed")
        val events = mutableListOf<Boolean>()
        val collector = launch { viewModel.addEvents.collect(events::add) }

        viewModel.insertNow()
        advanceUntilIdle()

        assertTrue(historyDao.items.isEmpty())
        assertEquals(listOf(false), events)
        assertFalse(viewModel.isAdding.value)
        collector.cancel()
    }

    @Test
    fun insertNowCanBeUsedAgainAfterCompletion() = runTest {
        val events = mutableListOf<Boolean>()
        val collector = launch { viewModel.addEvents.collect(events::add) }

        viewModel.insertNow()
        advanceUntilIdle()
        viewModel.insertNow()
        advanceUntilIdle()

        assertEquals(2, historyDao.items.size)
        assertEquals(listOf(true, true), events)
        assertFalse(viewModel.isAdding.value)
        collector.cancel()
    }
}
