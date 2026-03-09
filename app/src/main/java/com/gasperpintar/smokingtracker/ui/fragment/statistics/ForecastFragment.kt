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
import java.time.LocalDateTime
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
        val interval = GraphInterval.YEARLY
        val current = LocalDateTime.now()
        val mainCount = 12

        lifecycleScope.launch {
            val historyList = historyRepository.getEntries(date = current)

            val mainRaw = (0 until mainCount).map { index ->
                val date = current.minusMonths(index.toLong())
                val quantity = historyList.count {
                    it.createdAt.monthValue == date.monthValue && it.createdAt.year == date.year
                }
                GraphEntry(date = date.toLocalDate().withDayOfMonth(1), quantity = quantity)
            }.reversed()

            val forecast = calculateForecast(data = mainRaw)

            binding.forecastGraphView.setData(
                data = mainRaw,
                forecast = forecast,
                graphInterval = interval
            )
        }
    }

    private fun calculateForecast(
        data: List<GraphEntry>
    ): List<GraphEntry> {
        if (data.isEmpty()) {
            return emptyList()
        }

        val knownX = data.indices.map {
            (it + 1).toDouble()
        }

        val knownY = data.map {
            it.quantity.toDouble()
        }

        val averageX = knownX.average()
        val averageY = knownY.average()

        var numerator = 0.0
        var denominator = 0.0
        for (i in data.indices) {
            numerator += (knownX[i] - averageX) * (knownY[i] - averageY)
            denominator += (knownX[i] - averageX) * (knownX[i] - averageX)
        }

        val b = if (denominator != 0.0) {
            numerator / denominator
        } else {
            0.0
        }
        val a = averageY - b * averageX

        val forecastSteps = 12
        val lastDate = data.last().date

        return (1..forecastSteps).map { index ->
            val targetX = knownX.size.toDouble() + index
            val forecastY = a + b * targetX
            val finalQuantity = forecastY.roundToInt().coerceAtLeast(0)
            val forecastDate = lastDate.plusMonths(index.toLong()).withDayOfMonth(1)
            GraphEntry(quantity = finalQuantity, date = forecastDate)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}