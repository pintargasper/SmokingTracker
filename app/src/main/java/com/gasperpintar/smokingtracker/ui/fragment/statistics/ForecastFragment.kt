package com.gasperpintar.smokingtracker.ui.fragment.statistics

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.ForecastViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsForecastBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch

class ForecastFragment : Base<FragmentStatisticsForecastBinding>(
    bindingInflater = FragmentStatisticsForecastBinding::inflate
) {

    private val viewModel: ForecastViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    @Override
    override fun initialize() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadForecast()
            showContent()
        }
    }

    private suspend fun loadForecast() = binding.apply {
        val state = viewModel.getForecast()
        if (state.data.isEmpty()) {
            return@apply
        }

        forecastGraphView.setData(
            data = state.data,
            forecast = state.forecast,
            graphInterval = state.interval,
            isForecast = true
        )
    }
}