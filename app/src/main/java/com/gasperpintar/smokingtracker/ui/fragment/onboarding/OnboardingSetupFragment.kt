package com.gasperpintar.smokingtracker.ui.fragment.onboarding

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentOnboardingSetupBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch

class OnboardingSetupFragment : Base<FragmentOnboardingSetupBinding>(
    bindingInflater = FragmentOnboardingSetupBinding::inflate
) {

    private val viewModel: SettingsViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            setup()
            showContent()
        }
    }

    private suspend fun setup() = binding.apply {
        val state = viewModel.getState()

         dayEndLayout.setOnClickListener {
            DialogManager.showEndDayDialog(context = requireActivity(), settings = state.settings) { dayEndMinutes ->
                viewLifecycleOwner.lifecycleScope.launch {
                    state.settings = state.settings.copy(dayEndMinutes = dayEndMinutes)
                    viewModel.updateSettings(state.settings)
                }
            }
        }

        elapsedTimeWarningLayout.setOnClickListener {
            DialogManager.showElapsedTimeWarningDialog(
                context = requireActivity(),
                warningMinutes = state.settings.elapsedTimeWarningMinutes
            ) { warningMinutes ->
                viewLifecycleOwner.lifecycleScope.launch {
                    state.settings = state.settings.copy(elapsedTimeWarningMinutes = warningMinutes)
                    viewModel.updateSettings(state.settings)
                }
            }
        }

        currencyLayout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                DialogManager.showCurrencyDialog(context = requireActivity(), settings = state.settings) { currency, custom ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        state.settings = state.settings.copy(currency = currency, customCurrency = custom)
                        viewModel.updateSettings(state.settings)
                    }
                }
            }
        }

        costsLayout.setOnClickListener {
            DialogManager.showCostsDialog(context = requireActivity(), currency = state.settings.currency) { entry1, entry2, _ ->
                when {
                    entry1 != null -> viewModel.deleteCost(entry1)
                    entry2 != null -> viewModel.addCost(entry2)
                }
                viewModel.getState().costs
            }
        }
    }
}