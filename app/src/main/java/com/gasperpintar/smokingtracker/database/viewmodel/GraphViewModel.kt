package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphState
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.LocalDate
import java.time.LocalDateTime

class GraphViewModel(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private var selectedDate: LocalDate? = null
    private var endMinutes: Int? = null

    fun previous(previousUnit: (LocalDate) -> LocalDate) {
        selectedDate = previousUnit(selectedDate!!)
    }

    fun next(nextUnit: (LocalDate) -> LocalDate) {
        selectedDate = nextUnit(selectedDate!!)
    }

    suspend fun getEntries(): GraphState {
        val dayEndMinutes = settingsRepository.get()!!.dayEndMinutes
        if (selectedDate == null) selectedDate = TimeHelper.dayDate(dayEndMinutes)
        if (endMinutes != dayEndMinutes) {
            selectedDate = TimeHelper.updateDayDate(
                selectedDate = selectedDate!!,
                oldDayEndMinutes = endMinutes ?: 0,
                newDayEndMinutes = dayEndMinutes
            )
            endMinutes = dayEndMinutes
        }

        val (dailyStart, dailyEnd) = TimeHelper.getDay(date = selectedDate!!, dayEndMinutes = dayEndMinutes)
        val (weeklyStart, weeklyEnd) = TimeHelper.getWeek(date = selectedDate!!, dayEndMinutes = dayEndMinutes)
        val (monthlyStart, monthlyEnd) = TimeHelper.getMonth(date = selectedDate!!, dayEndMinutes = dayEndMinutes)
        val (yearlyStart, yearlyEnd) = TimeHelper.getYear(date = selectedDate!!, dayEndMinutes = dayEndMinutes)

        val dailyHistory = historyRepository.getBetween(start = dailyStart, end = dailyEnd)
        val weeklyHistory = historyRepository.getBetween(start = weeklyStart, end = weeklyEnd)
        val monthlyHistory = historyRepository.getBetween(start = monthlyStart, end = monthlyEnd)
        val yearlyHistory = historyRepository.getBetween(start = yearlyStart, end = yearlyEnd)

        return GraphState(
            selectedDate = selectedDate!!,
            dailyEntries = createHourlyEntries(history = dailyHistory, start = dailyStart),
            weeklyEntries = createDailyEntries(history = weeklyHistory, start = weeklyStart, end = weeklyEnd),
            monthlyEntries = createDailyEntries(history = monthlyHistory, start = monthlyStart, end = monthlyEnd),
            yearlyEntries = createYearlyEntries(history = yearlyHistory, start = yearlyStart),
            dailyCount = dailyHistory.size,
            weeklyCount = weeklyHistory.size,
            monthlyCount = monthlyHistory.size,
            yearlyCount = yearlyHistory.size,
            dayEndMinutes = dayEndMinutes
        )
    }

    private fun createHourlyEntries(
        history: List<HistoryEntity>,
        start: LocalDateTime
    ): List<GraphEntry> {
        return (0..23).map { index ->
            val hourStart = start.plusHours(index.toLong())
            GraphEntry(
                quantity = history.count { it.createdAt in hourStart..<hourStart.plusHours(1) },
                date = hourStart
            )
        }
    }

    private fun createDailyEntries(
        history: List<HistoryEntity>,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<GraphEntry> {
        return generateSequence(seed = start) { dayStart ->
            val nextDayStart = dayStart.plusDays(1)
            if (nextDayStart < end) nextDayStart else null
        }.map { dayStart ->
            GraphEntry(
                quantity = history.count { it.createdAt >= dayStart && it.createdAt < dayStart.plusDays(1) },
                date = dayStart
            )
        }.toList()
    }

    private fun createYearlyEntries(
        history: List<HistoryEntity>,
        start: LocalDateTime
    ): List<GraphEntry> {
        return (0..11).map { index ->
            val monthStart = start.plusMonths(index.toLong())
            GraphEntry(
                quantity = history.count { it.createdAt in monthStart..<monthStart.plusMonths(1) },
                date = monthStart
            )
        }
    }
}