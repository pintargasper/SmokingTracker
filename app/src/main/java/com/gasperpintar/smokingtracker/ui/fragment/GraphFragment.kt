package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatLocalized
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        initialize()
        loadGraphs()

        return binding.root
    }

    @Override
    override fun onResume() {
        super.onResume()
        loadGraphs()
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() = binding.apply {
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
    }

    private fun setupNavigation(
        previous: View,
        next: View,
        previousUnit: (LocalDate) -> LocalDate,
        nextUnit: (LocalDate) -> LocalDate
    ) {
        previous.setOnClickListener {
            viewModel.previous(previousUnit)
            loadGraphs()
        }

        next.setOnClickListener {
            viewModel.next(nextUnit)
            loadGraphs()
        }
    }

    private fun loadGraphs() {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getEntries()
            updateDaily(state)
            updateWeekly(state)
            updateMonthly(state)
            updateYearly(state)
        }
    }

    private fun updateDaily(
        state: GraphState
    ) = binding.apply {
        val (start, _) = TimeHelper.getDay(date = state.selectedDate)
        currentDateDaily.text = start.toLocalDate().formatLocalized()
        graphDaily.text = getString(R.string.graph_daily, state.dailyCount)
        graphViewDaily.setData(data = state.dailyEntries, graphInterval = GraphInterval.DAILY)
    }

    private fun updateWeekly(
        state: GraphState
    ) = binding.apply {
        val (start, end) = TimeHelper.getWeek(date = state.selectedDate)
        currentDateWeekly.text = LocalizationHelper.formatWeekRange(start = start.toLocalDate(), end = end.toLocalDate())
        graphWeekly.text = getString(R.string.graph_weekly, state.weeklyCount)
        graphViewWeekly.setData(data = state.weeklyEntries, graphInterval = GraphInterval.WEEKLY)
    }

    private fun updateMonthly(
        state: GraphState
    ) = binding.apply {
        val (start, _) = TimeHelper.getMonth(date = state.selectedDate)
        currentDateMonthly.text =
            String.format(
                Locale.getDefault(), "%s %d",
                LocalizationHelper.getMonthName(month = start.month),
                start.year
            )
        graphMonthly.text = getString(R.string.graph_monthly, state.monthlyCount)
        graphViewMonthly.setData(data = state.monthlyEntries, graphInterval = GraphInterval.MONTHLY)
    }

    private fun updateYearly(
        state: GraphState
    ) = binding.apply {
        val (start, _) = TimeHelper.getYear(date = state.selectedDate)
        currentDateYearly.text = start.year.toString()
        graphYearly.text = getString(R.string.graph_yearly, state.yearlyCount)
        graphViewYearly.setData(data = state.yearlyEntries, graphInterval = GraphInterval.YEARLY)
    }
}