package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.SmokingTrackerApp
import com.gasperpintar.smokingtracker.database.viewmodel.GraphViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.GraphUiState
import com.gasperpintar.smokingtracker.databinding.FragmentGraphBinding
import com.gasperpintar.smokingtracker.di.AppModelFactory
import com.gasperpintar.smokingtracker.type.GraphInterval
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels {
        AppModelFactory(appContainer = (requireActivity().application as SmokingTrackerApp).appContainer)
    }

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        initialize()
        observeState()

        return binding.root
    }

    @Override
    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        setupNavigation(
            previous = binding.previousDayDaily,
            next = binding.nextDayDaily,
            previousUnit = { it.minusDays(1) },
            nextUnit = { it.plusDays(1) }
        )

        setupNavigation(
            previous = binding.previousDayWeekly,
            next = binding.nextDayWeekly,
            previousUnit = { it.minusWeeks(1) },
            nextUnit = { it.plusWeeks(1) }
        )

        setupNavigation(
            previous = binding.previousDayMonthly,
            next = binding.nextDayMonthly,
            previousUnit = { it.minusMonths(1) },
            nextUnit = { it.plusMonths(1) }
        )

        setupNavigation(
            previous = binding.previousDayYearly,
            next = binding.nextDayYearly,
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
            val currentDate = viewModel.uiState.value.selectedDate
            viewModel.selectDate(date = previousUnit(currentDate))
        }

        next.setOnClickListener {
            val currentDate = viewModel.uiState.value.selectedDate
            viewModel.selectDate(date = nextUnit(currentDate))
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateDaily(state = state)
                updateWeekly(state = state)
                updateMonthly(state = state)
                updateYearly(state = state)
            }
        }
    }

    private fun updateDaily(
        state: GraphUiState
    ) {
        val (start, _) = TimeHelper.getDay(date = state.selectedDate)

        binding.currentDateDaily.text = LocalizationHelper.formatDate(date = start.toLocalDate())
        binding.graphDaily.text = getString(R.string.graph_daily, state.dailyCount)
        binding.graphViewDaily.setData(data = state.dailyEntries, graphInterval = GraphInterval.DAILY)
    }

    private fun updateWeekly(
        state: GraphUiState
    ) {
        val (start, end) = TimeHelper.getWeek(date = state.selectedDate)

        binding.currentDateWeekly.text = LocalizationHelper.formatWeekRange(start = start.toLocalDate(), end = end.toLocalDate())
        binding.graphWeekly.text = getString(R.string.graph_weekly, state.weeklyCount)
        binding.graphViewWeekly.setData(data = state.weeklyEntries, graphInterval = GraphInterval.WEEKLY)
    }

    private fun updateMonthly(
        state: GraphUiState
    ) {
        val (start, _) = TimeHelper.getMonth(date = state.selectedDate)

        binding.currentDateMonthly.text =
            String.format(
                Locale.getDefault(), "%s %d",
                LocalizationHelper.getMonthName(context = requireContext(), start.month),
                start.year
            )
        binding.graphMonthly.text = getString(R.string.graph_monthly, state.monthlyCount)
        binding.graphViewMonthly.setData(data = state.monthlyEntries, graphInterval = GraphInterval.MONTHLY)
    }

    private fun updateYearly(
        state: GraphUiState
    ) {
        val (start, _) = TimeHelper.getYear(date = state.selectedDate)

        binding.currentDateYearly.text = start.year.toString()
        binding.graphYearly.text = getString(R.string.graph_yearly, state.yearlyCount)
        binding.graphViewYearly.setData(data = state.yearlyEntries, graphInterval = GraphInterval.YEARLY)
    }
}