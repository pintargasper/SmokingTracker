package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.ForecastState
import com.gasperpintar.smokingtracker.type.GraphInterval
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class ForecastViewModel(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    suspend fun getState(): ForecastState {
        val current = LocalDateTime.now()
        val dayEndMinutes = settingsRepository.get()!!.dayEndMinutes
        val history = historyRepository.getEntries(date = current)

        if (history.isEmpty()) return ForecastState()
        val oldestRecord = history.minByOrNull { it.createdAt } ?: return ForecastState()
        val interval = determineGraphInterval(oldestRecord = oldestRecord.createdAt, current = current)

        val mainCount = when (interval) {
            GraphInterval.HOURLY -> Duration.between(oldestRecord.createdAt, current).toHours().toInt() + 5
            GraphInterval.DAILY -> Duration.between(oldestRecord.createdAt, current).toDays().toInt() + 7
            GraphInterval.WEEKLY -> ((Duration.between(oldestRecord.createdAt, current).toDays() / 7) + 1).toInt()
            else -> 12
        }

        val historyGrouped = history.groupBy {
            normalizeDate(date = it.createdAt, interval = interval, dayEndMinutes = dayEndMinutes)
        }

        val main = (0 until mainCount).map { index ->
            val stepsBack = (mainCount - 1 - index).toLong()
            val date = getDateForInterval(current = current, interval = interval, stepsBack = stepsBack, dayEndMinutes = dayEndMinutes)
            val count = historyGrouped[date]?.size ?: 0
            GraphEntry(date = date, quantity = count)
        }
        val forecast = calculateForecast(data = main, interval = interval, dayEndMinutes = dayEndMinutes)
        return ForecastState(data = main, forecast = forecast, interval = interval)
    }

    private fun calculateForecast(
        data: List<GraphEntry>,
        interval: GraphInterval,
        dayEndMinutes: Int
    ): List<GraphEntry> {
        if (data.isEmpty()) return emptyList()

        val forecastData = if (interval == GraphInterval.MONTHLY) data.dropLast(n = 1) else data
        if (forecastData.size < 2) return emptyList()

        val knownX = forecastData.indices.map { (it + 1).toDouble() }
        val knownY = forecastData.map { it.quantity.toDouble() }

        val avgX = knownX.average()
        val avgY = knownY.average()

        val numerator = knownX.zip(other = knownY).sumOf { (x, y) -> (x - avgX) * (y - avgY) }
        val denominator = knownX.sumOf { x -> (x - avgX) * (x - avgX) }

        val b = if (denominator != 0.0) numerator / denominator else 0.0
        val a = avgY - b * avgX

        val lastDate = data.last().date
        return (1..12).map { index ->
            val targetX = knownX.size.toDouble() + index
            val forecastY = (a + b * targetX).roundToInt().coerceAtLeast(minimumValue = 0)

            val rawForecastDate = when (interval) {
                GraphInterval.HOURLY -> lastDate.plusHours(index.toLong())
                GraphInterval.DAILY -> lastDate.plusDays(index.toLong())
                GraphInterval.WEEKLY -> lastDate.plusWeeks(index.toLong())
                GraphInterval.MONTHLY -> lastDate.plusMonths(index.toLong())
                else -> lastDate
            }
            val forecastDate = normalizeDate(date = rawForecastDate, interval = interval, dayEndMinutes = dayEndMinutes)
            GraphEntry(quantity = forecastY, date = forecastDate)
        }
    }

    private fun determineGraphInterval(
        oldestRecord: LocalDateTime,
        current: LocalDateTime
    ): GraphInterval {
        val hours = Duration.between(oldestRecord, current).toHours()
        return when {
            hours < 24 -> GraphInterval.HOURLY
            hours < 24 * 7 -> GraphInterval.DAILY
            oldestRecord.plusMonths(1).isAfter(current) -> GraphInterval.WEEKLY
            else -> GraphInterval.MONTHLY
        }
    }

    private fun getDateForInterval(
        current: LocalDateTime,
        interval: GraphInterval,
        stepsBack: Long,
        dayEndMinutes: Int
    ): LocalDateTime {
        val date = when (interval) {
            GraphInterval.HOURLY -> current.minusHours(stepsBack)
            GraphInterval.DAILY -> TimeHelper.dayDate(dateTime = current, dayEndMinutes = dayEndMinutes).minusDays(stepsBack).atStartOfDay()
            GraphInterval.WEEKLY -> TimeHelper.dayDate(dateTime = current, dayEndMinutes = dayEndMinutes).minusWeeks(stepsBack).atStartOfDay()
            GraphInterval.MONTHLY -> TimeHelper.dayDate(dateTime = current, dayEndMinutes = dayEndMinutes).minusMonths(stepsBack).withDayOfMonth(1).atStartOfDay()
            else -> current
        }
        return normalizeDate(date = date, interval = interval, dayEndMinutes = dayEndMinutes)
    }

    private fun normalizeDate(
        date: LocalDateTime,
        interval: GraphInterval,
        dayEndMinutes: Int
    ): LocalDateTime {
        return when (interval) {
            GraphInterval.HOURLY -> {
                val offset = dayEndMinutes.toLong()
                date.minusMinutes(offset).truncatedTo(ChronoUnit.HOURS).plusMinutes(offset)
            }
            GraphInterval.DAILY -> TimeHelper.dayDate(dateTime = date, dayEndMinutes = dayEndMinutes).atStartOfDay()
            GraphInterval.WEEKLY -> TimeHelper.dayDate(dateTime = date, dayEndMinutes = dayEndMinutes).atStartOfDay()
            GraphInterval.MONTHLY ->
                TimeHelper.dayDate(dateTime = date, dayEndMinutes = dayEndMinutes).withDayOfMonth(1).atStartOfDay()
            else -> date
        }
    }
}