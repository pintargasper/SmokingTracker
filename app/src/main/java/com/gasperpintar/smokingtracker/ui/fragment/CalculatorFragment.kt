package com.gasperpintar.smokingtracker.ui.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.CalculatorViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.CalculatorState
import com.gasperpintar.smokingtracker.databinding.FragmentCalculatorBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch

class CalculatorFragment : Base<FragmentCalculatorBinding>(
    bindingInflater = FragmentCalculatorBinding::inflate
) {

    private val appContainer by lazy {
        (requireActivity().application as Application).container
    }

    private val viewModel: CalculatorViewModel by viewModels {
        ModelFactory(container = appContainer)
    }

    @Override
    override fun initialize() = binding.apply {
        inputStartDate.setOnClickListener {
            DialogManager.showDatePickerDialog(context = requireActivity()) { date ->
                inputStartDate.setText(viewModel.setStartDate(date))
            }
        }

        inputEndDate.setOnClickListener {
            DialogManager.showDatePickerDialog(context = requireActivity()) { date ->
                inputEndDate.setText(viewModel.setEndDate(date))
            }
        }

        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        buttonCalculate.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                calculate()
            }
        }

        showContent()
    }

    private suspend fun calculate() = binding.apply {
        val state = viewModel.getState(
            dailyCigarettes = inputDailyCigarettes.text.toString().toIntOrNull() ?: 0,
            cigarettesPerPack = inputCigarettesPerPack.text.toString().toIntOrNull() ?: 20,
            packPrice = inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0
        )
        showResultDialog(state = state)
    }

    private fun showResultDialog(state: CalculatorState) {
        DialogManager.showResultDialog(
            context = requireActivity(),
            values = Triple(state.totalCost, state.totalTimeMinutes, state.totalCigarettes) to state.currency,
            formatTime = { minutes ->
                TimeHelper.formatTime(resources = resources, totalMinutes = minutes)
            }
        )
    }
}