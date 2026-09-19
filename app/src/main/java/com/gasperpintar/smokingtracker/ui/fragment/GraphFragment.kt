package com.gasperpintar.smokingtracker.ui.fragment

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.viewmodel.GraphViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphState
import com.gasperpintar.smokingtracker.databinding.FragmentGraphBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.GraphInterval
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

class GraphFragment : Base<FragmentGraphBinding>(
    bindingInflater = FragmentGraphBinding::inflate
) {

    private val viewModel: GraphViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() = binding.apply {
        setupNavigation(
            previous = previousDayDaily,
            next = nextDayDaily,
            previousUnit = { it.minusDays(1) },
            nextUnit = { it.plusDays(1) }
        )

        setupNavigation(
            previous = previousDayWeekly,
            next = nextDayWeekly,
            previousUnit = { it.minusWeeks(1) },
            nextUnit = { it.plusWeeks(1) }
        )

        setupNavigation(
            previous = previousDayMonthly,
            next = nextDayMonthly,
            previousUnit = { it.minusMonths(1) },
            nextUnit = { it.plusMonths(1) }
        )

        setupNavigation(
            previous = previousDayYearly,
            next = nextDayYearly,
            previousUnit = { it.minusYears(1) },
            nextUnit = { it.plusYears(1) }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            loadGraphs()
            showContent()
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            loadGraphs()
        }
    }

    private fun setupNavigation(
        previous: View,
        next: View,
        previousUnit: (LocalDate) -> LocalDate,
        nextUnit: (LocalDate) -> LocalDate
    ) {
        previous.setOnClickListener {
            viewModel.previous(previousUnit)
            viewLifecycleOwner.lifecycleScope.launch {
                loadGraphs()
            }
        }

        next.setOnClickListener {
            viewModel.next(nextUnit)
            viewLifecycleOwner.lifecycleScope.launch {
                loadGraphs()
            }
        }
    }

    private suspend fun loadGraphs() {
        val state = viewModel.getState()
        updateDaily(state)
        updateWeekly(state)
        updateMonthly(state)
        updateYearly(state)
    }

    private fun updateDaily(
        state: GraphState
    ) = binding.apply {
        val (start, _) = TimeHelper.getDay(date = state.selectedDate, dayEndMinutes = state.dayEndMinutes)
        currentDateDaily.text = LocalizationHelper.formatDateRange(start = start.toLocalDate(), end = null, skeleton = "dMMMM")
        graphDaily.text = getString(R.string.graph_daily, state.dailyCount)
        graphViewDaily.setData(data = state.dailyEntries, graphInterval = GraphInterval.DAILY)
    }

    private fun updateWeekly(
        state: GraphState
    ) = binding.apply {
        val (start, end) = TimeHelper.getWeek(date = state.selectedDate, dayEndMinutes = state.dayEndMinutes)
        currentDateWeekly.text = LocalizationHelper.formatDateRange(start = start.toLocalDate(), end = end.toLocalDate())
        graphWeekly.text = getString(R.string.graph_weekly, state.weeklyCount)
        graphViewWeekly.setData(data = state.weeklyEntries, graphInterval = GraphInterval.WEEKLY)
    }

    private fun updateMonthly(
        state: GraphState
    ) = binding.apply {
        val (start, _) = TimeHelper.getMonth(date = state.selectedDate, dayEndMinutes = state.dayEndMinutes)
        currentDateMonthly.text = LocalizationHelper.formatDateRange(start = start.toLocalDate(), end = null, skeleton = "MMMM yyyy")
        graphMonthly.text = getString(R.string.graph_monthly, state.monthlyCount)
        graphViewMonthly.setData(data = state.monthlyEntries, graphInterval = GraphInterval.MONTHLY)
    }

    private fun updateYearly(
        state: GraphState
    ) = binding.apply {
        val (start, _) = TimeHelper.getYear(date = state.selectedDate, dayEndMinutes = state.dayEndMinutes)
        currentDateYearly.text = start.year.toString()
        graphYearly.text = getString(R.string.graph_yearly, state.yearlyCount)
        graphViewYearly.setData(data = state.yearlyEntries, graphInterval = GraphInterval.YEARLY)
    }
}