package com.gasperpintar.smokingtracker.ui.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentTrackingBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.model.TriggerJson
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import kotlinx.coroutines.launch
import java.time.LocalTime

class TrackingFragment : Base<FragmentTrackingBinding>(
    bindingInflater = FragmentTrackingBinding::inflate
) {

    private val viewModel: SettingsViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            setup()
            showContent()
        }
    }

    private suspend fun setup() = binding.apply {
        val state = viewModel.getState()

        dayEndValue.text = LocalTime.of(state.settings.dayEndMinutes / 60, state.settings.dayEndMinutes % 60).toString()
        elapsedTimeWarningValue.text = requireContext().resources.getQuantityString(
            R.plurals.time_minutes,
            state.settings.elapsedTimeWarningMinutes,
            state.settings.elapsedTimeWarningMinutes
        )
        triggerImage.setImageResource(TriggerJson.Icon.valueOf(state.settings.defaultTrigger.name).drawableResource)
        triggerValue.text = getString(TriggerJson.Title.valueOf(state.settings.defaultTrigger.name).stringResource)

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
                    elapsedTimeWarningValue.text = requireContext().resources.getQuantityString(R.plurals.time_minutes, warningMinutes, warningMinutes)
                }
            }
        }

        triggerLayout.setOnClickListener {
            DialogManager.showTriggersDialog(context = requireActivity(), defaultTrigger = state.settings.defaultTrigger) { defaultTrigger ->
                viewLifecycleOwner.lifecycleScope.launch {
                    state.settings = state.settings.copy(defaultTrigger = defaultTrigger)
                    viewModel.updateSettings(state.settings)
                    triggerValue.text = getString(TriggerJson.Title.valueOf(defaultTrigger.name).stringResource)
                    triggerImage.setImageResource(TriggerJson.Icon.valueOf(defaultTrigger.name).drawableResource)
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