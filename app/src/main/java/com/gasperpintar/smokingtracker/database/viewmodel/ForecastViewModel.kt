package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.ForecastState
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.type.GraphInterval
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class ForecastViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    suspend fun getForecast(): ForecastState {
        val current = LocalDateTime.now()
        val history = historyRepository.getEntries(date = current)

        if (history.isEmpty()) return ForecastState()
        val oldestRecord = history.minByOrNull { it.createdAt } ?: return ForecastState()
        val interval = determineGraphInterval(oldestRecord.createdAt, current)

        val mainCount = when (interval) {
            GraphInterval.HOURLY -> Duration.between(oldestRecord.createdAt, current).toHours().toInt() + 5
            GraphInterval.DAILY -> Duration.between(oldestRecord.createdAt, current).toDays().toInt() + 7
            GraphInterval.WEEKLY -> ((Duration.between(oldestRecord.createdAt, current).toDays() / 7) + 1).toInt()
            else -> 12
        }

        val historyGrouped = history.groupBy { normalizeDate(it.createdAt, interval) }
        val main = (0 until mainCount).map { index ->
            val stepsBack = (mainCount - 1 - index).toLong()
            val date = normalizeDate(getDateForInterval(current, interval, stepsBack), interval)
            val count = historyGrouped[date]?.size ?: 0
            GraphEntry(date = date, quantity = count)
        }
        val forecast = calculateForecast(data = main, interval)
        return ForecastState(data = main, forecast = forecast, interval = interval)
    }

    private fun calculateForecast(
        data: List<GraphEntry>,
        interval: GraphInterval
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

            val forecastDate = when (interval) {
                GraphInterval.HOURLY -> lastDate.plusHours(index.toLong())
                GraphInterval.DAILY -> lastDate.plusDays(index.toLong())
                GraphInterval.WEEKLY -> lastDate.plusWeeks(index.toLong())
                GraphInterval.MONTHLY -> lastDate.plusMonths(index.toLong()).withDayOfMonth(1)
                else -> lastDate
            }
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
        stepsBack: Long
    ): LocalDateTime = when (interval) {
        GraphInterval.HOURLY -> current.minusHours(stepsBack)
        GraphInterval.DAILY -> current.minusDays(stepsBack)
        GraphInterval.WEEKLY -> current.minusWeeks(stepsBack)
        GraphInterval.MONTHLY -> current.minusMonths(stepsBack)
        else -> current
    }

    private fun normalizeDate(
        date: LocalDateTime,
        interval: GraphInterval
    ): LocalDateTime = when (interval) {
        GraphInterval.HOURLY -> date.truncatedTo(ChronoUnit.HOURS)
        GraphInterval.DAILY -> date.truncatedTo(ChronoUnit.DAYS)
        GraphInterval.WEEKLY -> date.truncatedTo(ChronoUnit.DAYS)
        GraphInterval.MONTHLY -> date.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
        else -> date
    }
}