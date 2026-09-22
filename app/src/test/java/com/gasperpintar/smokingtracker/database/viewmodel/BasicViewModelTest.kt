package com.gasperpintar.smokingtracker.database.viewmodel

import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.fake.FakeCostsDao
import com.gasperpintar.smokingtracker.database.fake.FakeHistoryDao
import com.gasperpintar.smokingtracker.database.fake.FakeSettingsDao
import com.gasperpintar.smokingtracker.database.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class BasicViewModelTest {

    private lateinit var historyDao: FakeHistoryDao
    private lateinit var costsDao: FakeCostsDao
    private lateinit var settingsDao: FakeSettingsDao
    private lateinit var viewModel: BasicViewModel

    @Before
    fun setup() = runBlocking {
        historyDao = FakeHistoryDao()
        costsDao = FakeCostsDao()
        settingsDao = FakeSettingsDao()
        settingsDao.insert(entity = SettingsEntity.default())

        viewModel = BasicViewModel(
            historyRepository = HistoryRepository(historyDao = historyDao),
            costsRepository = CostsRepository(costDao = costsDao),
            settingsRepository = SettingsRepository(settingsDao = settingsDao)
        )
    }

    @Test
    fun getStateSumsSpentUsingPriceValidAtEachEntry() = runBlocking {
        insertCost(start = LocalDateTime.of(2026, 1, 1, 0, 0), end = LocalDateTime.of(2026, 2, 1, 0, 0), price = 0.25)
        insertCost(start = LocalDateTime.of(2026, 2, 1, 0, 0), end = LocalDateTime.of(2026, 3, 1, 0, 0), price = 0.30)
        insertHistory(
            LocalDateTime.of(2026, 1, 15, 10, 0),
            LocalDateTime.of(2026, 1, 15, 18, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 5, 12, 0)
        )

        val state = viewModel.getState()

        assertTrue(state.hasCosts)
        assertEquals(0.80, state.totalSpent, 0.0001)
        assertEquals("2026-01-15", state.mostExpensiveDay)
        assertEquals(0.50, state.mostExpensiveDaySpent, 0.0001)
    }

    @Test
    fun getStateAssignsEntriesBeforeDayEndToPreviousDay() = runBlocking {
        settingsDao.update(entity = settingsDao.get()!!.copy(dayEndMinutes = 120))
        insertCost(start = LocalDateTime.of(2026, 1, 1, 0, 0), end = LocalDateTime.of(2026, 2, 1, 0, 0), price = 0.25)
        insertHistory(
            LocalDateTime.of(2026, 1, 15, 12, 0),
            LocalDateTime.of(2026, 1, 16, 1, 0),
            LocalDateTime.of(2026, 1, 16, 1, 30)
        )

        val state = viewModel.getState()

        assertEquals("2026-01-15", state.mostExpensiveDay)
        assertEquals(0.75, state.mostExpensiveDaySpent, 0.0001)
    }

    @Test
    fun getStateWithoutCostsReportsNoSpending() = runBlocking {
        insertHistory(LocalDateTime.of(2026, 1, 15, 10, 0))

        val state = viewModel.getState()

        assertFalse(state.hasCosts)
        assertEquals(0.0, state.totalSpent, 0.0)
        assertNull(state.mostExpensiveDay)
        assertEquals(0.0, state.mostExpensiveDaySpent, 0.0)
    }

    @Test
    fun getStateCalculatesTodaySpent() = runBlocking {
        val today = LocalDate.now()
        insertCost(start = today.minusDays(1).atStartOfDay(), end = today.plusDays(1).atStartOfDay(), price = 0.4)
        insertHistory(today.atTime(12, 0), today.atTime(12, 30), today.minusDays(1).atTime(12, 0))

        val state = viewModel.getState()

        assertEquals(0.8, state.todaySpent, 0.0001)
    }

    @Test
    fun getStateReturnsCurrentStreakWhenLastEntryIsOld() = runBlocking {
        insertHistory(
            LocalDateTime.of(2020, 1, 1, 10, 0),
            LocalDateTime.of(2020, 1, 10, 10, 0),
            LocalDateTime.of(2020, 1, 11, 10, 0)
        )

        val state = viewModel.getState()

        assertEquals(LocalDateTime.of(2020, 1, 11, 10, 0), state.longestStreak?.first)
    }

    @Test
    fun getStateReturnsHistoricalStreakWhenItIsLongest() = runBlocking {
        val now = LocalDateTime.now()
        insertHistory(now.minusDays(30), now.minusDays(10), now.minusHours(1))

        val state = viewModel.getState()

        assertEquals(now.minusDays(30) to now.minusDays(10), state.longestStreak)
    }

    @Test
    fun getStateWithoutHistoryHasNoStreak() = runBlocking {
        assertNull(viewModel.getState().longestStreak)
    }

    @Test
    fun getStateReturnsDailyExtremesFromRepository() = runBlocking {
        historyDao.maxPerDay = CigarettesPerDay(dailySum = 15, day = "2026-01-15")
        historyDao.minPerDay = CigarettesPerDay(dailySum = 2, day = "2026-01-20")
        historyDao.averagePerDay = 7.5

        val state = viewModel.getState()

        assertEquals(15, state.maxCigarettes)
        assertEquals("2026-01-15", state.maxCigarettesDate)
        assertEquals(2, state.minCigarettes)
        assertEquals("2026-01-20", state.minCigarettesDate)
        assertEquals(7.5, state.averageCigarettes, 0.0)
    }

    private suspend fun insertCost(start: LocalDateTime, end: LocalDateTime, price: Double) {
        costsDao.insert(entity = CostEntity(id = 0, startDate = start, endDate = end, price = price))
    }

    private suspend fun insertHistory(vararg dates: LocalDateTime) {
        dates.forEach { historyDao.insert(entity = HistoryEntity(id = 0, lent = 0, createdAt = it)) }
    }
}
