package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphUiState
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class GraphViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate

            val (dailyStart, dailyEnd) = TimeHelper.getDay(date)
            val (weeklyStart, weeklyEnd) = TimeHelper.getWeek(date)
            val (monthlyStart, monthlyEnd) = TimeHelper.getMonth(date)
            val (yearlyStart, yearlyEnd) = TimeHelper.getYear(date)

            val dailyHistory = historyRepository.getBetween(start = dailyStart, end = dailyEnd)
            val weeklyHistory = historyRepository.getBetween(start = weeklyStart, end = weeklyEnd)
            val monthlyHistory = historyRepository.getBetween(start = monthlyStart, end = monthlyEnd)
            val yearlyHistory = historyRepository.getBetween(start = yearlyStart, end = yearlyEnd)

            _uiState.value = _uiState.value.copy(
                dailyEntries = createHourlyEntries(history = dailyHistory, start = dailyStart),
                weeklyEntries = createDailyEntries(history = weeklyHistory,start = weeklyStart, end = weeklyEnd),
                monthlyEntries = createDailyEntries(history = monthlyHistory, start = monthlyStart, end = monthlyEnd),
                yearlyEntries = createYearlyEntries(history = yearlyHistory, start = yearlyStart),

                dailyCount = dailyHistory.size,
                weeklyCount = weeklyHistory.size,
                monthlyCount = monthlyHistory.size,
                yearlyCount = yearlyHistory.size
            )
        }
    }

    fun selectDate(
        date: LocalDate
    ) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        load()
    }

    fun refresh() {
        load()
    }

    private fun createHourlyEntries(
        history: List<HistoryEntity>,
        start: LocalDateTime
    ): List<GraphEntry> {

        val hourlyCountMap = history.groupingBy { it.createdAt.hour }.eachCount()
        return (0..23).map { hour ->
            GraphEntry(
                quantity = hourlyCountMap[hour] ?: 0,
                date = start.withHour(hour).withMinute(0).withSecond(0).withNano(0)
            )
        }.dropLastWhile { it.quantity == 0 }
    }

    private fun createDailyEntries(
        history: List<HistoryEntity>,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<GraphEntry> {
        val startDate = start.toLocalDate()
        val endDate = end.toLocalDate()

        return generateSequence(seed = startDate) { day ->
            val nextDay = day.plusDays(1)
            if (nextDay <= endDate) nextDay else null
        }.map { day ->
            GraphEntry(
                quantity = history.count { it.createdAt.toLocalDate() == day },
                date = day.atStartOfDay()
            )
        }.toList().dropLastWhile { it.quantity == 0 }
    }

    private fun createYearlyEntries(
        history: List<HistoryEntity>,
        start: LocalDateTime
    ): List<GraphEntry> {
        val monthCountMap = history.groupingBy { it.createdAt.monthValue }.eachCount()
        return (1..12).map { monthNumber ->
            GraphEntry(
                quantity = monthCountMap[monthNumber] ?: 0,
                date = start.withMonth(monthNumber).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            )
        }.dropLastWhile { it.quantity == 0 }
    }
}