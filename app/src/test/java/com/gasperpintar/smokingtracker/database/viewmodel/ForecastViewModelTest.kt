package com.gasperpintar.smokingtracker.database.viewmodel

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.fake.FakeHistoryDao
import com.gasperpintar.smokingtracker.database.fake.FakeSettingsDao
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.ForecastState
import com.gasperpintar.smokingtracker.type.GraphInterval
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ForecastViewModelTest {

    private val now = LocalDateTime.now()
    private lateinit var historyDao: FakeHistoryDao
    private lateinit var viewModel: ForecastViewModel

    @Before
    fun setup() = runBlocking {
        historyDao = FakeHistoryDao()
        val settingsDao = FakeSettingsDao()
        settingsDao.insert(entity = SettingsEntity.default())

        viewModel = ForecastViewModel(
            historyRepository = HistoryRepository(historyDao = historyDao),
            settingsRepository = SettingsRepository(settingsDao = settingsDao)
        )
    }

    @Test
    fun getStateWithoutHistoryReturnsDefaultState() = runBlocking {
        assertEquals(ForecastState(), viewModel.getState())
    }

    @Test
    fun getStateUsesHourlyIntervalForRecentHistory() = runBlocking {
        insertHistory(now.minusHours(2), now.minusHours(1), now.minusMinutes(30))

        val state = viewModel.getState()

        assertEquals(GraphInterval.HOURLY, state.interval)
        assertEquals(3, state.data.sumOf { it.quantity })
    }

    @Test
    fun getStateUsesDailyIntervalForHistoryWithinWeek() = runBlocking {
        insertHistory(now.minusDays(3), now.minusDays(2), now.minusDays(1), now.minusDays(1).minusHours(1))

        val state = viewModel.getState()

        assertEquals(GraphInterval.DAILY, state.interval)
        assertEquals(4, state.data.sumOf { it.quantity })
    }

    @Test
    fun getStateUsesWeeklyIntervalAndCountsEveryDay() = runBlocking {
        insertHistory(*(0L..14L).map { now.minusDays(it).minusMinutes(1) }.toTypedArray())

        val state = viewModel.getState()

        assertEquals(GraphInterval.WEEKLY, state.interval)
        assertEquals(15, state.data.sumOf { it.quantity })
    }

    @Test
    fun getStateUsesMonthlyIntervalForOlderHistory() = runBlocking {
        insertHistory(now.minusDays(60), now.minusDays(40), now.minusDays(5))

        val state = viewModel.getState()

        assertEquals(GraphInterval.MONTHLY, state.interval)
        assertEquals(3, state.data.sumOf { it.quantity })
    }

    @Test
    fun getStateForecastsTwelveFuturePoints() = runBlocking {
        listOf(
            now.minusHours(2),
            now.minusDays(3),
            now.minusDays(14).minusMinutes(1),
            now.minusDays(60)
        ).forEach { oldest ->
            historyDao.deleteAll()
            insertHistory(oldest, now.minusMinutes(10))

            val state = viewModel.getState()

            assertEquals(12, state.forecast.size)
            assertTrue(state.forecast.all { it.quantity >= 0 })
            assertTrue(state.forecast.first().date > state.data.last().date)
            assertTrue(state.forecast.zipWithNext().all { (previous, current) -> current.date > previous.date })
        }
    }

    @Test
    fun getStateForecastFollowsIncreasingTrend() = runBlocking {
        val today = LocalDate.now()
        (0..4).forEach { daysBack ->
            repeat(times = 5 - daysBack) {
                historyDao.insert(entity = HistoryEntity(id = 0, lent = 0, createdAt = today.minusDays(daysBack.toLong()).atTime(0, 0, 1)))
            }
        }

        val state = viewModel.getState()

        assertEquals(GraphInterval.DAILY, state.interval)
        assertTrue(state.forecast.zipWithNext().all { (previous, current) -> current.quantity >= previous.quantity })
        assertTrue(state.forecast.last().quantity > state.forecast.first().quantity)
    }

    private suspend fun insertHistory(vararg dates: LocalDateTime) {
        dates.forEach { historyDao.insert(entity = HistoryEntity(id = 0, lent = 0, createdAt = it)) }
    }
}
