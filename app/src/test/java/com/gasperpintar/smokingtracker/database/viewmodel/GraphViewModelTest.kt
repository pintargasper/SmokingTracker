package com.gasperpintar.smokingtracker.database.viewmodel

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.fake.FakeHistoryDao
import com.gasperpintar.smokingtracker.database.fake.FakeSettingsDao
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GraphViewModelTest {

    private val selectedDate = LocalDate.of(2024, 2, 15)
    private lateinit var historyDao: FakeHistoryDao
    private lateinit var settingsDao: FakeSettingsDao
    private lateinit var viewModel: GraphViewModel

    @Before
    fun setup() = runBlocking {
        historyDao = FakeHistoryDao()
        settingsDao = FakeSettingsDao()
        settingsDao.insert(entity = SettingsEntity.default())

        viewModel = GraphViewModel(
            historyRepository = HistoryRepository(historyDao = historyDao),
            settingsRepository = SettingsRepository(settingsDao = settingsDao)
        )
    }

    @Test
    fun getStateCreatesEntriesForEachPeriod() = runBlocking {
        val state = stateFor(date = selectedDate)

        assertEquals(24, state.dailyEntries.size)
        assertEquals(LocalDateTime.of(2024, 2, 15, 0, 0), state.dailyEntries.first().date)
        assertEquals(7, state.weeklyEntries.size)
        assertEquals(LocalDateTime.of(2024, 2, 12, 0, 0), state.weeklyEntries.first().date)
        assertEquals(29, state.monthlyEntries.size)
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), state.monthlyEntries.first().date)
        assertEquals(12, state.yearlyEntries.size)
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), state.yearlyEntries.first().date)
    }

    @Test
    fun getStateCountsEntriesOfSelectedPeriods() = runBlocking {
        insertHistory(
            LocalDateTime.of(2024, 2, 12, 8, 0),
            LocalDateTime.of(2024, 2, 15, 10, 15),
            LocalDateTime.of(2024, 2, 15, 10, 45),
            LocalDateTime.of(2024, 2, 15, 23, 59, 59),
            LocalDateTime.of(2024, 2, 16, 0, 0),
            LocalDateTime.of(2024, 3, 1, 12, 0)
        )

        val state = stateFor(date = selectedDate)

        assertEquals(3, state.dailyCount)
        assertEquals(state.dailyCount, state.dailyEntries.sumOf { it.quantity })
        assertEquals(2, state.dailyEntries[10].quantity)
        assertEquals(5, state.weeklyCount)
        assertEquals(state.weeklyCount, state.weeklyEntries.sumOf { it.quantity })
        assertEquals(5, state.monthlyCount)
        assertEquals(state.monthlyCount, state.monthlyEntries.sumOf { it.quantity })
        assertEquals(6, state.yearlyCount)
        assertEquals(state.yearlyCount, state.yearlyEntries.sumOf { it.quantity })
    }

    @Test
    fun getStateStartsDayAtConfiguredDayEnd() = runBlocking {
        settingsDao.update(entity = settingsDao.get()!!.copy(dayEndMinutes = 120))
        insertHistory(LocalDateTime.of(2024, 2, 15, 1, 30), LocalDateTime.of(2024, 2, 16, 1, 30))

        val state = stateFor(date = selectedDate)

        assertEquals(LocalDateTime.of(2024, 2, 15, 2, 0), state.dailyEntries.first().date)
        assertEquals(1, state.dailyCount)
        assertEquals(1, state.dailyEntries.last().quantity)
    }

    @Test
    fun getStateKeepsPastDateWhenDayEndChanges() = runBlocking {
        stateFor(date = selectedDate)
        settingsDao.update(entity = settingsDao.get()!!.copy(dayEndMinutes = 120))

        assertEquals(selectedDate, viewModel.getState().selectedDate)
    }

    @Test
    fun previousAndNextMoveSelectedDate() = runBlocking {
        stateFor(date = selectedDate)

        viewModel.previous { it.minusWeeks(1) }
        assertEquals(selectedDate.minusWeeks(1), viewModel.getState().selectedDate)

        viewModel.next { it.plusMonths(1) }
        assertEquals(selectedDate.minusWeeks(1).plusMonths(1), viewModel.getState().selectedDate)
    }

    private suspend fun stateFor(date: LocalDate): GraphState {
        viewModel.getState()
        viewModel.next { date }
        return viewModel.getState()
    }

    private suspend fun insertHistory(vararg dates: LocalDateTime) {
        dates.forEach { historyDao.insert(entity = HistoryEntity(id = 0, lent = 0, createdAt = it)) }
    }
}