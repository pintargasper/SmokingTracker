package com.gasperpintar.smokingtracker.database.viewmodel

import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.fake.FakeSettingsDao
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone

class CalculatorViewModelTest {

    private val originalTimeZone = TimeZone.getDefault()
    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setup() = runBlocking {
        val settingsDao = FakeSettingsDao()
        settingsDao.insert(entity = SettingsEntity.default(currency = "$"))
        viewModel = CalculatorViewModel(settingsRepository = SettingsRepository(settingsDao = settingsDao))
    }

    @After
    fun teardown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun getStateWithoutDatesCalculatesSingleDay() = runBlocking {
        val state = viewModel.getState(dailyCigarettes = 20, cigarettesPerPack = 20, packPrice = 5.0)

        assertEquals(5.0, state.totalCost, 0.001)
        assertEquals(100, state.totalTimeMinutes)
        assertEquals(20, state.totalCigarettes)
        assertEquals("$", state.currency)
    }

    @Test
    fun getStateCalculatesInclusiveDayRange() = runBlocking {
        viewModel.setStartDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 1, hour = 10))
        viewModel.setEndDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 10, hour = 10))

        val state = viewModel.getState(dailyCigarettes = 20, cigarettesPerPack = 20, packPrice = 5.0)

        assertEquals(200, state.totalCigarettes)
        assertEquals(50.0, state.totalCost, 0.001)
        assertEquals(1_000, state.totalTimeMinutes)
    }

    @Test
    fun setStartDateAfterEndDateClampsRangeToSingleDay() = runBlocking {
        viewModel.setEndDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 10, hour = 10))
        viewModel.setStartDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 15, hour = 10))

        val state = viewModel.getState(dailyCigarettes = 20, cigarettesPerPack = 20, packPrice = 5.0)

        assertEquals(20, state.totalCigarettes)
    }

    @Test
    fun getStateCountsDaysAcrossDaylightSavingChange() = runBlocking {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Ljubljana"))

        viewModel.setStartDate(date = calendar(year = 2026, month = Calendar.MARCH, day = 28, hour = 10))
        viewModel.setEndDate(date = calendar(year = 2026, month = Calendar.MARCH, day = 30, hour = 10))

        val state = viewModel.getState(dailyCigarettes = 20, cigarettesPerPack = 20, packPrice = 5.0)

        assertEquals(60, state.totalCigarettes)
    }

    @Test
    fun getStateCountsDaysWhenEndDateIsSelectedFirst() = runBlocking {
        viewModel.setEndDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 10, hour = 9))
        viewModel.setStartDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 1, hour = 10))

        val state = viewModel.getState(dailyCigarettes = 20, cigarettesPerPack = 20, packPrice = 5.0)

        assertEquals(200, state.totalCigarettes)
    }

    @Test
    fun setStartDateReturnsFormattedDate() {
        val actual = viewModel.setStartDate(date = calendar(year = 2026, month = Calendar.JANUARY, day = 1, hour = 10))

        assertEquals(LocalizationHelper.formatDate(LocalDate.of(2026, 1, 1)), actual)
    }

    private fun calendar(year: Int, month: Int, day: Int, hour: Int): Calendar {
        return Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0, 0)
        }
    }
}
