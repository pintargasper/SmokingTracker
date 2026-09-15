package com.gasperpintar.smokingtracker.activity

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.CalculatorViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.CalculatorState
import com.gasperpintar.smokingtracker.databinding.ActivityCalculatorBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch

class CalculatorActivity : Base<ActivityCalculatorBinding>(
    bindingInflater = ActivityCalculatorBinding::inflate
) {

    private val appContainer by lazy { (application as Application).container }
    private val viewModel: CalculatorViewModel by viewModels {
        ModelFactory(application = application as Application, container = appContainer)
    }

    @Override
    override fun initialize() = binding.apply {
        inputStartDate.setOnClickListener {
            DialogManager.showDatePickerDialog(context = this@CalculatorActivity) { date ->
                inputStartDate.setText( viewModel.setStartDate(date))
            }
        }

        inputEndDate.setOnClickListener {
            DialogManager.showDatePickerDialog(context = this@CalculatorActivity) { date ->
                inputEndDate.setText( viewModel.setEndDate(date))
            }
        }

        buttonBack.setOnClickListener {
            finish()
        }

        buttonCalculate.setOnClickListener {
            lifecycleScope.launch {
                calculate()
            }
        }
    }

    private suspend fun calculate() = binding.apply {
        val state = viewModel.calculate(
            dailyCigarettes = inputDailyCigarettes.text.toString().toIntOrNull() ?: 0,
            cigarettesPerPack = inputCigarettesPerPack.text.toString().toIntOrNull() ?: 20,
            packPrice = inputPackPrice.text.toString().toDoubleOrNull() ?: 0.0
        )
        showResultDialog(state = state)
    }

    private fun showResultDialog(state: CalculatorState) {
        DialogManager.showResultDialog(
            context = this,
            totalCost = state.totalCost,
            totalTimeMinutes = state.totalTimeMinutes,
            totalCigarettes = state.totalCigarettes,
            currencyUnit = state.currency,
            formatTime = { minutes ->
                TimeHelper.formatTime(resources = resources, totalMinutes = minutes)
            }
        )
    }
}