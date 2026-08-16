package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.StatisticsActivity
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsForecastBinding
import com.gasperpintar.smokingtracker.model.GraphEntry
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.type.GraphInterval
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class ForecastFragment : Fragment() {

    private var _binding: FragmentStatisticsForecastBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyRepository: HistoryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsForecastBinding.inflate(inflater, container, false)

        val database = (requireActivity() as StatisticsActivity).database
        historyRepository = HistoryRepository(historyDao = database.historyDao())

        setup()

        return binding.root
    }

    private fun setup() {
        val current = LocalDateTime.now()

        lifecycleScope.launch {
            val historyList = historyRepository.getEntries(date = current)

            if (historyList.isEmpty()) {
                return@launch
            }

            val oldestRecord = historyList.minByOrNull {
                it.createdAt
            } ?: return@launch

            val interval = determineGraphInterval(oldestRecord = oldestRecord.createdAt, current = current)

            val mainCount = when (interval) {
                GraphInterval.HOURLY -> Duration.between(oldestRecord.createdAt, current).toHours().toInt() + 5
                GraphInterval.DAILY -> Duration.between(oldestRecord.createdAt, current).toDays().toInt() + 7
                GraphInterval.WEEKLY -> ((Duration.between(oldestRecord.createdAt, current).toDays() / 7) + 1).toInt()
                GraphInterval.MONTHLY -> 12
                else -> 12
            }

            val mainRaw = (0 until mainCount).map { index ->
                val stepsBack = (mainCount - 1 - index).toLong()
                val date = getDateForInterval(current, interval, stepsBack)

                GraphEntry(
                    date = normalizeDate(date, interval),
                    quantity = historyList.count {
                        isInInterval(recordDate = it.createdAt, date, interval)
                    }
                )
            }

            val forecast = calculateForecast(data = mainRaw, interval = interval)

            binding.forecastGraphView.setData(
                data = mainRaw,
                forecast = forecast,
                graphInterval = interval,
                isForecast = true
            )
        }
    }

    private fun calculateForecast(
        data: List<GraphEntry>,
        interval: GraphInterval
    ): List<GraphEntry> {
        if (data.isEmpty()) {
            return emptyList()
        }

        val forecastData = data.dropLast(n = (interval == GraphInterval.MONTHLY).takeIf { it }?.let { 1 } ?: 0)

        val knownX = forecastData.indices.map {
            (it + 1).toDouble()
        }

        val knownY = forecastData.map {
            it.quantity.toDouble()
        }

        val averageX = knownX.average()
        val averageY = knownY.average()

        var numerator = 0.0
        var denominator = 0.0
        for (index in forecastData.indices) {
            numerator += (knownX[index] - averageX) * (knownY[index] - averageY)
            denominator += (knownX[index] - averageX) * (knownX[index] - averageX)
        }

        val b = (denominator != 0.0).takeIf { it }?.let {
            numerator / denominator
        } ?: 0.0
        val a = averageY - b * averageX

        val forecastSteps = 12
        val lastDate = data.last().date

        return (1..forecastSteps).map { index ->
            val targetX = knownX.size.toDouble() + index
            val forecastY = a + b * targetX

            val finalQuantity = forecastY.roundToInt().coerceAtLeast(minimumValue = 0)

            val forecastDate = when (interval) {
                GraphInterval.HOURLY -> lastDate.plusHours(index.toLong())
                GraphInterval.DAILY -> lastDate.plusDays(index.toLong())
                GraphInterval.WEEKLY -> lastDate.plusWeeks(index.toLong())
                GraphInterval.MONTHLY -> lastDate.plusMonths(index.toLong()).withDayOfMonth(1)
                else -> lastDate
            }

            GraphEntry(quantity = finalQuantity, date = forecastDate)
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
    ): LocalDateTime {
        return when (interval) {
            GraphInterval.HOURLY -> current.minusHours(stepsBack)
            GraphInterval.DAILY -> current.minusDays(stepsBack)
            GraphInterval.WEEKLY -> current.minusWeeks(stepsBack)
            GraphInterval.MONTHLY -> current.minusMonths(stepsBack)
            else -> current
        }
    }

    private fun normalizeDate(
        date: LocalDateTime,
        interval: GraphInterval
    ): LocalDateTime {
        return when (interval) {
            GraphInterval.HOURLY -> date.withMinute(0).withSecond(0).withNano(0)
            GraphInterval.WEEKLY -> date.toLocalDate().atStartOfDay()
            GraphInterval.MONTHLY -> date.withDayOfMonth(1).toLocalDate().atStartOfDay()
            else -> date
        }
    }

    private fun isInInterval(
        recordDate: LocalDateTime,
        date: LocalDateTime,
        interval: GraphInterval
    ): Boolean =
        when (interval) {
            GraphInterval.HOURLY -> recordDate.truncatedTo(ChronoUnit.HOURS) == date.truncatedTo(ChronoUnit.HOURS)
            GraphInterval.DAILY -> recordDate.toLocalDate() == date.toLocalDate()
            GraphInterval.WEEKLY -> {
                val record = recordDate.toLocalDate()
                val start = date.toLocalDate()
                record >= start && record < start.plusWeeks(1)
            }
            GraphInterval.MONTHLY -> recordDate.year == date.year && recordDate.month == date.month
            else -> false
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}