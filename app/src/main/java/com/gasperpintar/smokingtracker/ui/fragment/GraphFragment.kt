package com.gasperpintar.smokingtracker.ui.fragment

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.database.viewmodel.GraphViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphState
import com.gasperpintar.smokingtracker.databinding.FragmentGraphBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.Graph
import com.gasperpintar.smokingtracker.ui.view.chart.Base as ChartBase
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters

class GraphFragment : Base<FragmentGraphBinding>(
    bindingInflater = FragmentGraphBinding::inflate
) {

    private val viewModel: GraphViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    private var selectedPeriod = Graph.Period.DAILY
    private var selectedType = Graph.Type.NORMAL
    private var lastGraphData: ChartBase.Data? = null

    @Override
    override fun initialize() = binding.apply {
        setupSelection()
        setupNavigation()

        updateStatsVisibility()
        showContent()
        refreshGraph()
    }

    @Override
    override fun onResume() {
        super.onResume()
        refreshGraph()
    }

    private fun refreshGraph() {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getState(isLent = selectedType == Graph.Type.BORROWED)
            updateGraph(state)
        }
    }

    private fun setupSelection() = binding.apply {
        periodToggleGroup.addOnButtonCheckedListener {
            _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedPeriod = when (checkedId) {
                R.id.period_daily -> Graph.Period.DAILY
                R.id.period_weekly -> Graph.Period.WEEKLY
                R.id.period_monthly -> Graph.Period.MONTHLY
                R.id.period_yearly -> Graph.Period.YEARLY
                else -> selectedPeriod
            }
            updateStatsVisibility()
            refreshGraph()
        }

        typeToggleGroup.addOnButtonCheckedListener {
            _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedType = when (checkedId) {
                R.id.type_normal -> Graph.Type.NORMAL
                R.id.type_borrowed -> Graph.Type.BORROWED
                else -> selectedType
            }
            refreshGraph()
        }
    }

    private fun setupNavigation() = binding.apply {
        previousGraph.setOnClickListener {
            navigate(forward = false)
        }

        nextGraph.setOnClickListener {
            navigate(forward = true)
        }
    }

    private fun navigate(forward: Boolean) {
        val amount = if (forward) 1L else -1L
        val dateChange: (LocalDate) -> LocalDate = { date ->
            when (selectedPeriod) {
                Graph.Period.DAILY -> date.plusDays(amount)
                Graph.Period.WEEKLY -> date.plusWeeks(amount)
                Graph.Period.MONTHLY -> date.plusMonths(amount)
                Graph.Period.YEARLY -> date.plusYears(amount)
            }
        }

        if (forward) viewModel.next(nextUnit = dateChange)
        else viewModel.previous(previousUnit = dateChange)
        refreshGraph()
    }

    private fun updateStatsVisibility() = binding.apply {
        dailyStats.isVisible = selectedPeriod == Graph.Period.DAILY
        weeklyStats.isVisible = selectedPeriod == Graph.Period.WEEKLY
        monthlyStats.isVisible = selectedPeriod == Graph.Period.MONTHLY
        yearlyStats.isVisible = selectedPeriod == Graph.Period.YEARLY
    }

    private fun mapEntries(
        entries: List<GraphEntry>,
        label: (GraphEntry) -> String
    ): List<ChartBase.Entry> {
        return entries.map {
            ChartBase.Entry(label = label(it), value = it.quantity.toFloat())
        }
    }

    private fun formatConsumptionChange(
        value: Double?
    ): String {
        return value?.let {
            (if (it > 0) "+" else "") + LocalizationHelper.formatPercentage(value = it)
        } ?: "—"
    }

    private fun updateGraph(
        state: GraphState
    ) = binding.apply {
        updateDate(state)
        updateStatsVisibility()

        val (count, entries) = when (selectedPeriod) {
            Graph.Period.DAILY -> {
                consumptionPeak.text = state.busiestHour?.let {
                    LocalizationHelper.formatTime(LocalTime.of(it, 0))
                } ?: "—"

                consumptionChangeDaily.text = formatConsumptionChange(value = state.dailyConsumptionChange)
                state.dailyCount to mapEntries(state.dailyEntries) {
                    LocalizationHelper.formatTime(it.date.toLocalTime())
                }
            }

            Graph.Period.WEEKLY -> {
                average.text = state.weeklyAverage?.let {
                    LocalizationHelper.formatNumber(value = it)
                } ?: "—"

                consumptionChangeWeekly.text = formatConsumptionChange(value =  state.weeklyConsumptionChange)
                state.weeklyCount to mapEntries(state.weeklyEntries) {
                    LocalizationHelper.getDayOfWeekName(it.date.dayOfWeek, style = TextStyle.SHORT)
                }
            }

            Graph.Period.MONTHLY -> {
                busiestWeek.text = state.busiestWeekStart?.let {
                    LocalizationHelper.formatDateRange(start = it, end = it.plusDays(6))
                } ?: "—"

                consumptionChangeMonthly.text = formatConsumptionChange(value = state.monthlyConsumptionChange)
                state.monthlyCount to mapEntries(state.monthlyEntries) {
                    it.date.dayOfMonth.toString()
                }
            }

            Graph.Period.YEARLY -> {
                busiestMonth.text = state.busiestMonth?.let {
                    LocalizationHelper.getMonthName(month = it)
                } ?: "—"

                bestMonth.text = state.bestMonth?.let {
                    LocalizationHelper.getMonthName(month = it)
                } ?: "—"

                state.yearlyCount to mapEntries(state.yearlyEntries) {
                    LocalizationHelper.formatDateRange(start = it.date.toLocalDate(), skeleton = "MMM")
                }
            }
        }

        val title = when (selectedPeriod) {
            Graph.Period.DAILY -> getString(R.string.graph_daily, count)
            Graph.Period.WEEKLY -> getString(R.string.graph_weekly, count)
            Graph.Period.MONTHLY -> getString(R.string.graph_monthly, count)
            Graph.Period.YEARLY -> getString(R.string.graph_yearly, count)
        }

        val data = ChartBase.Data(title = title, entries = entries)
        if (lastGraphData != data) {
            graphView.setData(data)
            lastGraphData = data
        }
    }

    private fun updateDate(
        state: GraphState
    ) = binding.apply {
        val date = state.selectedDate
        currentDateGraph.text = when (selectedPeriod) {
            Graph.Period.DAILY -> {
                val (start, _) = TimeHelper.getDay(date = date, dayEndMinutes = state.dayEndMinutes)
                LocalizationHelper.formatDateRange(start = start.toLocalDate(), end = null, skeleton = "dMMMM")
            }

            Graph.Period.WEEKLY -> {
                val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                LocalizationHelper.formatDateRange(start = weekStart, end = weekStart.plusDays(6))
            }

            Graph.Period.MONTHLY -> {
                val monthStart = date.with(TemporalAdjusters.firstDayOfMonth())
                LocalizationHelper.formatDateRange(start = monthStart, end = null, skeleton = "MMMM")
            }

            Graph.Period.YEARLY -> {
                val yearStart = date.with(TemporalAdjusters.firstDayOfYear())
                LocalizationHelper.formatDateRange(start = yearStart, end = null, skeleton = "y")
            }
        }
    }
}