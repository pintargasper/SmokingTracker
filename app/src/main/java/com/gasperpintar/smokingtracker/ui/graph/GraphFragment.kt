package com.gasperpintar.smokingtracker.ui.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.databinding.FragmentGraphBinding
import com.gasperpintar.smokingtracker.model.GraphEntry
import com.gasperpintar.smokingtracker.type.GraphInterval
import com.gasperpintar.smokingtracker.utils.Helper
import com.gasperpintar.smokingtracker.utils.Helper.getMonthName
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: Lazy<AppDatabase>
    private lateinit var selectedDate: LocalDate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = lazy { (requireActivity() as MainActivity).database }
        selectedDate = LocalDate.now()

        setup()
        loadGraphs()

        return root
    }

    private fun setup() {
        setupNavigation(
            previous = binding.previousDayWeekly,
            next = binding.nextDayWeekly,
            previousUnit = { it.minusWeeks(1) },
            nextUnit = { it.plusWeeks(1) },
            loader = ::loadWeeklyData
        )

        setupNavigation(
            previous = binding.previousDayMonthly,
            next = binding.nextDayMonthly,
            previousUnit = { it.minusMonths(1) },
            nextUnit = { it.plusMonths(1) },
            loader = ::loadMonthlyData
        )

        setupNavigation(
            previous = binding.previousDayYearly,
            next = binding.nextDayYearly,
            previousUnit = { it.minusYears(1) },
            nextUnit = { it.plusYears(1) },
            loader = ::loadYearlyData
        )
    }

    private fun setupNavigation(
        previous: View,
        next: View,
        previousUnit: (LocalDate) -> LocalDate,
        nextUnit: (LocalDate) -> LocalDate,
        loader: suspend () -> Unit
    ) {
        previous.setOnClickListener {
            selectedDate = previousUnit(selectedDate)
            lifecycleScope.launch { loader() }
        }

        next.setOnClickListener {
            selectedDate = nextUnit(selectedDate)
            lifecycleScope.launch { loader() }
        }
    }

    private fun loadGraphs() {
        lifecycleScope.launch {
            loadWeeklyData()
            loadMonthlyData()
            loadYearlyData()
        }
    }

    private suspend fun loadWeeklyData() {
        loadGraph(
            getDateRange = { Helper.getWeek(selectedDate) },
            labelFormatter = { start, end ->
                getString(
                    R.string.graph_weekly_date,
                    start.dayOfMonth,
                    start.monthValue,
                    end.dayOfMonth,
                    end.monthValue,
                    start.year
                )
            },
            interval = GraphInterval.WEEKLY
        )
    }

    private suspend fun loadMonthlyData() {
        loadGraph(
            getDateRange = { Helper.getMonth(selectedDate) },
            labelFormatter = { start, _ ->
                getString(
                    R.string.graph_monthly_date,
                    context?.getMonthName(start.month),
                    start.year
                )
            },
            interval = GraphInterval.MONTHLY
        )
    }

    private suspend fun loadYearlyData() {
        loadGraph(
            getDateRange = { Helper.getYear(selectedDate) },
            labelFormatter = { start, _ ->
                getString(R.string.graph_yearly_date, start.year)
            },
            interval = GraphInterval.YEARLY
        )
    }

    private suspend fun loadGraph(
        getDateRange: () -> Pair<LocalDateTime, LocalDateTime>,
        labelFormatter: (LocalDate, LocalDate) -> String,
        interval: GraphInterval
    ) {
        val (startDateTime, endDateTime) = getDateRange()
        val startDate = startDateTime.toLocalDate()
        val endDate = endDateTime.toLocalDate()

        val currentDateTextView = when (interval) {
            GraphInterval.WEEKLY -> binding.currentDateWeekly
            GraphInterval.MONTHLY -> binding.currentDateMonthly
            GraphInterval.YEARLY -> binding.currentDateYearly
        }
        currentDateTextView.text = labelFormatter(startDate, endDate)

        val historyList = database.value.historyDao().getHistoryBetween(startDateTime, endDateTime)

        val entries: List<GraphEntry> = when (interval) {
            GraphInterval.YEARLY -> {
                val monthCountMap = historyList.groupingBy { it.createdAt.monthValue }.eachCount()
                (1..12).map { monthNumber ->
                    val count = monthCountMap[monthNumber] ?: 0
                    GraphEntry(quantity = count, date = startDate.withMonth(monthNumber).withDayOfMonth(1))
                }.dropLastWhile { it.quantity == 0 }
            }
            else -> {
                generateSequence(seed = startDate) { day ->
                    val nextDay = day.plusDays(1)
                    if (nextDay <= endDate) nextDay else null
                }.map { day ->
                    GraphEntry(quantity = historyList.count { it.createdAt.toLocalDate() == day }, date = day)
                }.toList().dropLastWhile { it.quantity == 0 }
            }
        }

        val currentGraphView = when (interval) {
            GraphInterval.WEEKLY -> binding.graphViewWeekly
            GraphInterval.MONTHLY -> binding.graphViewMonthly
            GraphInterval.YEARLY -> binding.graphViewYearly
        }
        currentGraphView.setData(data = entries, graphInterval = interval)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}