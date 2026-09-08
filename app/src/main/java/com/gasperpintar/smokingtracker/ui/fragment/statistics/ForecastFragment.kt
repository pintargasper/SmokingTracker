package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.ForecastViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsForecastBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import kotlinx.coroutines.launch

class ForecastFragment : Fragment() {

    private var _binding: FragmentStatisticsForecastBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForecastViewModel by viewModels {
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
        _binding = FragmentStatisticsForecastBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        loadForecast()
    }

    private fun loadForecast() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getForecast()
            if (state.data.isEmpty()) {
                return@launch
            }

            forecastGraphView.setData(
                data = state.data,
                forecast = state.forecast,
                graphInterval = state.interval,
                isForecast = true
            )
        }
    }
}