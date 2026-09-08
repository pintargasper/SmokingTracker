package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.viewmodel.BasicViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBasicBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.util.Locale

class BasicFragment : Fragment() {

    private var _binding: FragmentStatisticsBasicBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BasicViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBasicBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        loadStatistics()
    }

    private fun loadStatistics() = binding.apply {
        val settingsRepository = (requireActivity().application as Application).container.settingsRepository
        val fallbackDuration = resources.getQuantityString(R.plurals.time_minutes, 0, 0)

        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getStatistics()
            textMaxCigarettes.text = state.maxCigarettes.toString()
            textMaxCigarettesDate.text = LocalizationHelper.formatLoggedDate(resources, day = state.maxCigarettesDate)

            textMinCigarettes.text = state.minCigarettes.toString()
            textMinCigarettesDate.text = LocalizationHelper.formatLoggedDate(resources, day = state.minCigarettesDate)

            textAverageCigarettes.text =
                String.format(Locale.getDefault(), "%.2f", state.averageCigarettes)
            textTotalCigarettes.text = state.totalCigarettes.toString()

            textSinceFirstEntry.text = state.firstRecordDate?.let {
                TimeHelper.getDurationString(resources = resources, start = it)
            } ?: fallbackDuration

            textLongestStreak.text = state.longestStreak?.let { (start, end) ->
                TimeHelper.getDurationString(resources = resources, start = start, end = end)
            } ?: fallbackDuration

            statisticsTitleInstructions.isVisible = !state.hasCosts

            textTotalSpent.text = LocalizationHelper.formatMoney(settingsRepository, value = state.totalSpent)
            todaySpent.text = LocalizationHelper.formatMoney(settingsRepository, value = state.todaySpent)
            monthSpent.text = LocalizationHelper.formatMoney(settingsRepository, value = state.monthSpent)

            mostExpensiveDay.text = LocalizationHelper.formatMoney(settingsRepository, value = state.mostExpensiveDaySpent)
            mostExpensiveDayDate.text = LocalizationHelper.formatLoggedDate(resources, state.mostExpensiveDay)
        }
    }
}