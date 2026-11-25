package com.gasperpintar.smokingtracker.ui.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.ForecastViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentForecastBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import kotlinx.coroutines.launch

class ForecastFragment : Base<FragmentForecastBinding>(
    bindingInflater = FragmentForecastBinding::inflate
) {

    private val viewModel: ForecastViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loadForecast()
            showContent()
        }
    }

    private suspend fun loadForecast() = binding.apply {
        val state = viewModel.getState()
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