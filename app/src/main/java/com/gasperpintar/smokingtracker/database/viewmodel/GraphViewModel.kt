package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphState
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

class GraphViewModel(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var selectedDate: LocalDate? = null
    private var endMinutes: Int? = null

    fun previous(previousUnit: (LocalDate) -> LocalDate) {
        selectedDate = previousUnit(requireNotNull(selectedDate))
    }

    fun next(nextUnit: (LocalDate) -> LocalDate) {
        selectedDate = nextUnit(requireNotNull(selectedDate))
    }

    private suspend fun getFilteredHistory(
        start: LocalDateTime,
        end: LocalDateTime,
        isLent: Boolean
    ): List<HistoryEntry> {
        return historyRepository.getBetween(start = start, end = end).filter {
            it.isLent == isLent
        }
    }

    private suspend fun getCount(
        start: LocalDateTime,
        end: LocalDateTime,
        isLent: Boolean
    ): Int {
        return getFilteredHistory(start, end, isLent).size
    }

    private fun calculatePercentageChange(
        currentCount: Int,
        previousCount: Int
    ): Double? {
        if (currentCount == 0 || previousCount == 0) return null
        return ((currentCount - previousCount).toDouble() / previousCount) * 100.0
    }

    suspend fun getState(isLent: Boolean = false): GraphState {
        val dayEndMinutes = requireNotNull(settingsRepository.get()).dayEndMinutes

        if (selectedDate == null) selectedDate = TimeHelper.dayDate(dayEndMinutes)
        if (endMinutes != dayEndMinutes) {
            selectedDate = TimeHelper.updateDayDate(
                selectedDate = requireNotNull(selectedDate),
                oldDayEndMinutes = endMinutes ?: 0,
                newDayEndMinutes = dayEndMinutes
            )
            endMinutes = dayEndMinutes
        }

        val date = requireNotNull(selectedDate)

        val (dailyStart, dailyEnd) = TimeHelper.getDay(date, dayEndMinutes)
        val (weeklyStart, weeklyEnd) = TimeHelper.getWeek(date, dayEndMinutes)
        val (monthlyStart, monthlyEnd) = TimeHelper.getMonth(date, dayEndMinutes)
        val (yearlyStart, yearlyEnd) = TimeHelper.getYear(date, dayEndMinutes)

        val dailyHistory = getFilteredHistory(dailyStart, dailyEnd, isLent)
        val weeklyHistory = getFilteredHistory(weeklyStart, weeklyEnd, isLent)
        val monthlyHistory = getFilteredHistory(monthlyStart, monthlyEnd, isLent)
        val yearlyHistory = getFilteredHistory(yearlyStart, yearlyEnd, isLent)

        val (previousDailyStart, previousDailyEnd) = TimeHelper.getDay(date.minusDays(1), dayEndMinutes)
        val (previousWeeklyStart, previousWeeklyEnd) = TimeHelper.getWeek(date.minusWeeks(1), dayEndMinutes)
        val (previousMonthlyStart, previousMonthlyEnd) = TimeHelper.getMonth(date.minusMonths(1), dayEndMinutes)

        val previousDailyCount = getCount(previousDailyStart, previousDailyEnd, isLent)
        val previousWeeklyCount = getCount(previousWeeklyStart, previousWeeklyEnd, isLent)
        val previousMonthlyCount = getCount(previousMonthlyStart, previousMonthlyEnd, isLent)

        val dailyConsumptionChange = calculatePercentageChange(currentCount = dailyHistory.size, previousCount = previousDailyCount)
        val weeklyConsumptionChange = calculatePercentageChange(currentCount = weeklyHistory.size, previousCount = previousWeeklyCount)
        val monthlyConsumptionChange = calculatePercentageChange(currentCount = monthlyHistory.size, previousCount = previousMonthlyCount)

        val dailyEntries = (0..23).map { hour ->
            val hourStart = dailyStart.plusHours(hour.toLong())
            val hourEnd = hourStart.plusHours(1)
            GraphEntry(
                quantity = dailyHistory.count {
                    it.createdAt in hourStart..<hourEnd
                },
                date = hourStart
            )
        }

        val weeklyEntries = (0..6).map { day ->
            val currentDate = weeklyStart.toLocalDate().plusDays(day.toLong())
            val (currentStart, currentEnd) = TimeHelper.getDay(currentDate, dayEndMinutes)
            GraphEntry(
                quantity = weeklyHistory.count {
                    it.createdAt in currentStart..<currentEnd
                },
                date = currentStart
            )
        }

        val daysInMonth = monthlyStart.toLocalDate().lengthOfMonth()
        val monthlyEntries = (0 until daysInMonth).map { day ->
            val currentDate = monthlyStart.toLocalDate().plusDays(day.toLong())
            val (currentStart, currentEnd) = TimeHelper.getDay(currentDate, dayEndMinutes)
            GraphEntry(
                quantity = monthlyHistory.count {
                    it.createdAt in currentStart..<currentEnd
                },
                date = currentStart
            )
        }

        val yearlyEntries = (0..11).map { month ->
            val currentDate = yearlyStart.toLocalDate().withDayOfMonth(1).plusMonths(month.toLong())
            val (currentStart, currentEnd) = TimeHelper.getMonth(currentDate, dayEndMinutes)
            GraphEntry(
                quantity = yearlyHistory.count {
                    it.createdAt in currentStart..<currentEnd
                },
                date = currentStart
            )
        }

        val busiestHour = dailyEntries.filter { it.quantity > 0 }.maxByOrNull { it.quantity }?.date?.hour
        val weeklyAverage = if (weeklyHistory.isEmpty()) null
        else weeklyEntries.sumOf { it.quantity }.toDouble() / weeklyEntries.size

        val busiestWeekStart = monthlyEntries.groupBy {
            it.date.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        }.mapValues { (_, entries) ->
            entries.sumOf { it.quantity }
        }.filterValues { it > 0 }.maxByOrNull { it.value }?.key

        val busiestMonth = yearlyEntries.filter { it.quantity > 0 }.maxByOrNull { it.quantity }?.date?.month
        val currentMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
        val bestMonth = yearlyEntries.filter {
            it.quantity > 0 && it.date <= currentMonth
        }.minByOrNull { it.quantity }?.date?.month

        return GraphState(
            selectedDate = date,
            dailyEntries = dailyEntries,
            weeklyEntries = weeklyEntries,
            monthlyEntries = monthlyEntries,
            yearlyEntries = yearlyEntries,
            dailyCount = dailyHistory.size,
            weeklyCount = weeklyHistory.size,
            monthlyCount = monthlyHistory.size,
            yearlyCount = yearlyHistory.size,
            dayEndMinutes = dayEndMinutes,
            busiestHour = busiestHour,
            weeklyAverage = weeklyAverage,
            dailyConsumptionChange = dailyConsumptionChange,
            weeklyConsumptionChange = weeklyConsumptionChange,
            monthlyConsumptionChange = monthlyConsumptionChange,
            busiestWeekStart = busiestWeekStart,
            busiestMonth = busiestMonth,
            bestMonth = bestMonth
        )
    }
}