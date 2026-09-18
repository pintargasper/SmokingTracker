package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.BasicState
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.Duration
import java.time.LocalDateTime

class BasicViewModel(
    private val historyRepository: HistoryRepository,
    private val costsRepository: CostsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    suspend fun getState(): BasicState {
        val history = historyRepository.getAll()
        val costs = costsRepository.getAll()
        val dayEndMinutes = settingsRepository.get()!!.dayEndMinutes

        val max = historyRepository.getMaxCigarettesPerDay()
        val min = historyRepository.getMinCigarettesPerDay()

        val (mostExpensiveDay, mostExpensiveSpent) = getMostExpensiveDay(costs = costs, history = history, dayEndMinutes = dayEndMinutes)
        return BasicState(
            maxCigarettes = max?.dailySum ?: 0,
            maxCigarettesDate = max?.day,
            minCigarettes = min?.dailySum ?: 0,
            minCigarettesDate = min?.day,
            averageCigarettes = historyRepository.getAverageCigarettesPerDay(),
            totalCigarettes = historyRepository.getTotalCigarettes(),
            firstRecordDate = historyRepository.getFirstRecordDate(),
            longestStreak = getLongestTime(history),
            hasCosts = costs.isNotEmpty(),
            totalSpent = getTotalSpent(history, costs),
            todaySpent = getTodaySpent(costs, dayEndMinutes),
            monthSpent = getThisMonthSpent(costs, dayEndMinutes),
            mostExpensiveDay = mostExpensiveDay,
            mostExpensiveDaySpent = mostExpensiveSpent
        )
    }

    private fun getLongestTime(
        history: List<HistoryEntity>
    ): Pair<LocalDateTime, LocalDateTime>? {
        val sorted = history.takeIf { it.isNotEmpty() }?.sortedBy { it.createdAt } ?: return null
        val historicalMax = sorted.zipWithNext().maxByOrNull { (previous, current) ->
            Duration.between(previous.createdAt, current.createdAt)
        }?.let { it.first.createdAt to it.second.createdAt }

        val historicalDuration = historicalMax?.let { Duration.between(it.first, it.second) } ?: Duration.ZERO
        val currentStreak = sorted.last().createdAt to LocalDateTime.now()
        val currentDuration = Duration.between(currentStreak.first, currentStreak.second)
        return if (currentDuration > historicalDuration) currentStreak else historicalMax
    }

    private fun getTotalSpent(
        history: List<HistoryEntity>,
        costs: List<CostEntity>
    ): Double {
        return history.sumOf { resolveCostAtTime(it.createdAt, costs) }
    }

    private suspend fun getTodaySpent(
        costs: List<CostEntity>,
        dayEndMinutes: Int
    ): Double {
        val (start, end) = TimeHelper.getDay(date = TimeHelper.dayDate(dayEndMinutes), dayEndMinutes = dayEndMinutes)
        return historyRepository.getBetween(start = start, end = end).sumOf {
            resolveCostAtTime(it.createdAt, costs)
        }
    }

    private suspend fun getThisMonthSpent(
        costs: List<CostEntity>,
        dayEndMinutes: Int
    ): Double {
        val (start, end) = TimeHelper.getMonth(date = TimeHelper.dayDate(dayEndMinutes), dayEndMinutes = dayEndMinutes)
        return historyRepository.getBetween(start = start, end = end).sumOf {
            resolveCostAtTime(it.createdAt, costs)
        }
    }

    private fun resolveCostAtTime(
        time: LocalDateTime,
        costs: List<CostEntity>
    ): Double = costs.firstOrNull { !time.isBefore(it.startDate) && time.isBefore(it.endDate) }?.price ?: 0.0

    private fun getMostExpensiveDay(
        costs: List<CostEntity>,
        history: List<HistoryEntity>,
        dayEndMinutes: Int
    ): Pair<String?, Double> {
        if (costs.isEmpty() || history.isEmpty()) return null to 0.0

        return history
            .groupBy { TimeHelper.dayDate(dateTime = it.createdAt, dayEndMinutes = dayEndMinutes) }
            .mapValues { (_, daily) ->
                daily.sumOf { resolveCostAtTime(it.createdAt, costs) }
            }
            .maxByOrNull { it.value }
            ?.let { it.key.toString() to it.value }
            ?: (null to 0.0)
    }
}