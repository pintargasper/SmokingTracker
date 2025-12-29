package com.gasperpintar.smokingtracker.ui.fragment

import android.annotation.SuppressLint
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
            previous = binding.previousDayDaily,
            next = binding.nextDayDaily,
            previousUnit = { it.minusDays(1) },
            nextUnit = { it.plusDays(1) },
            loader = ::loadDailyData
        )

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
            loadDailyData()
            loadWeeklyData()
            loadMonthlyData()
            loadYearlyData()
        }
    }

    @SuppressLint("DefaultLocale")
    private suspend fun loadDailyData() {
        loadGraph(
            getDateRange = { Helper.getDay(selectedDate) },
            labelFormatter = { start, _ ->
                String.format(
                    "%02d.%02d.%04d",
                    start.dayOfMonth,
                    start.monthValue,
                    start.year
                )
            },
            interval = GraphInterval.DAILY
        )
    }

    @SuppressLint("DefaultLocale")
    private suspend fun loadWeeklyData() {
        loadGraph(
            getDateRange = { Helper.getWeek(selectedDate) },
            labelFormatter = { start, end ->
                String.format(
                    $$"%1$d.%2$d/%3$d.%4$d %5$d",
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

    @SuppressLint("DefaultLocale")
    private suspend fun loadMonthlyData() {
        loadGraph(
            getDateRange = { Helper.getMonth(selectedDate) },
            labelFormatter = { start, _ ->
                String.format(
                    $$"%1$s %2$d",
                    context?.getMonthName(start.month),
                    start.year
                )
            },
            interval = GraphInterval.MONTHLY
        )
    }

    @SuppressLint("DefaultLocale")
    private suspend fun loadYearlyData() {
        loadGraph(
            getDateRange = { Helper.getYear(selectedDate) },
            labelFormatter = { start, _ ->
                String.format($$"%1$d", start.year)
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
            GraphInterval.DAILY -> binding.currentDateDaily
            GraphInterval.WEEKLY -> binding.currentDateWeekly
            GraphInterval.MONTHLY -> binding.currentDateMonthly
            GraphInterval.YEARLY -> binding.currentDateYearly
        }
        currentDateTextView.text = labelFormatter(startDate, endDate)

        val historyList = database.value.historyDao().getHistoryBetween(startDateTime, endDateTime)

        val entries: List<GraphEntry> = when (interval) {

            GraphInterval.DAILY -> {
                val hourlyCountMap: Map<Int, Int> =
                    historyList.groupingBy { it.createdAt.hour }.eachCount()

                (0..23).map { hour: Int ->
                    GraphEntry(
                        quantity = hourlyCountMap[hour] ?: 0,
                        date = startDate.atTime(hour, 0).toLocalDate()
                    )
                }.dropLastWhile { it.quantity == 0 }
            }

            GraphInterval.YEARLY -> {
                val monthCountMap = historyList.groupingBy { it.createdAt.monthValue }.eachCount()
                (1..12).map { monthNumber ->
                    val count = monthCountMap[monthNumber] ?: 0
                    GraphEntry(
                        quantity = count,
                        date = startDate.withMonth(monthNumber).withDayOfMonth(1)
                    )
                }.dropLastWhile { it.quantity == 0 }
            }

            else -> {
                generateSequence(seed = startDate) { day ->
                    val nextDay = day.plusDays(1)
                    if (nextDay <= endDate) nextDay else null
                }.map { day ->
                    GraphEntry(
                        quantity = historyList.count {
                            it.createdAt.toLocalDate() == day
                        },
                        date = day
                    )
                }.toList().dropLastWhile { it.quantity == 0 }
            }
        }

        val intervalViewsAndStrings = mapOf(
            GraphInterval.DAILY to Pair(binding.graphDaily, R.string.graph_daily),
            GraphInterval.WEEKLY to Pair(binding.graphWeekly, R.string.graph_weekly),
            GraphInterval.MONTHLY to Pair(binding.graphMonthly, R.string.graph_monthly),
            GraphInterval.YEARLY to Pair(binding.graphYearly, R.string.graph_yearly)
        )
        val (currentTextView, stringResId) = intervalViewsAndStrings[interval]!!
        currentTextView.text = getString(stringResId, historyList.size)

        val currentGraphView = when (interval) {
            GraphInterval.DAILY -> binding.graphViewDaily
            GraphInterval.WEEKLY -> binding.graphViewWeekly
            GraphInterval.MONTHLY -> binding.graphViewMonthly
            GraphInterval.YEARLY -> binding.graphViewYearly
        }
        currentGraphView.setData(data = entries, graphInterval = interval)
    }

    override fun onResume() {
        super.onResume()
        loadGraphs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}