package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.CalculatorState
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.Duration
import java.util.Calendar

class CalculatorViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private val perMinute = 5
    private val millisInDay = Duration.ofDays(1).toMillis()

    fun setStartDate(date: Calendar): String {
        val (start, end, text) = TimeHelper.applySelectedDate(
            startDate = startDate,
            endDate = endDate,
            selectedDate = date,
            isStartDate = true
        )
        startDate = start
        endDate = end
        return text
    }

    fun setEndDate(date: Calendar): String {
        val (start, end, text) = TimeHelper.applySelectedDate(
            startDate = startDate,
            endDate = endDate,
            selectedDate = date,
            isStartDate = false
        )
        startDate = start
        endDate = end
        return text
    }

    suspend fun calculate(
        dailyCigarettes: Int,
        cigarettesPerPack: Int,
        packPrice: Double
    ): CalculatorState {
        val days = calculateDays()
        val dailyCost = (dailyCigarettes.toDouble() / cigarettesPerPack) * packPrice
        val dailyTimeMinutes = dailyCigarettes * perMinute
        return CalculatorState(
            totalCost = dailyCost * days,
            currency = settingsRepository.get()!!.currency,
            totalTimeMinutes = dailyTimeMinutes * days,
            totalCigarettes = dailyCigarettes * days
        )
    }

    private fun calculateDays(): Int {
        if (startDate == null || endDate == null) return 1
        if (endDate!!.before(startDate)) return 1
        val diffMillis = endDate!!.timeInMillis - startDate!!.timeInMillis
        return (diffMillis / millisInDay).toInt() + 1
    }
}