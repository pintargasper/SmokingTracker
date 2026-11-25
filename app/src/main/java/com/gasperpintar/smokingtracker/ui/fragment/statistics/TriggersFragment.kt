package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.viewmodel.TriggerViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsTriggersBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.model.TriggerJson
import com.gasperpintar.smokingtracker.ui.fragment.Base
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch

class TriggersFragment : Base<FragmentStatisticsTriggersBinding>(
    bindingInflater = FragmentStatisticsTriggersBinding::inflate
) {

    private val viewModel: TriggerViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            loadTriggers()
            showContent()
        }
    }

    private suspend fun loadTriggers() {
        val state = viewModel.getState()

        state.mostCommon?.let { trigger ->
            val title = getString(TriggerJson.Title.valueOf(trigger.type.name).stringResource)
            binding.mostCommonTriggerValue.text = title
            binding.mostCommonTriggerCount.text = resources.getQuantityString(
                R.plurals.achieved_times,
                trigger.value,
                trigger.value
            )
        } ?: binding.apply {
            mostCommonTriggerValue.text = getString(R.string.statistics_triggers_empty)
        }

        state.triggers.forEach { trigger ->
            val item = layoutInflater.inflate(R.layout.item_statistics_trigger, binding.triggerContainer, false)
            val title = getString(TriggerJson.Title.valueOf(trigger.type.name).stringResource)
            val icon = TriggerJson.Icon.valueOf(trigger.type.name).drawableResource

            item.findViewById<MaterialTextView>(R.id.trigger_name).text = title
            item.findViewById<ShapeableImageView>(R.id.trigger_icon).setImageResource(icon)
            item.findViewById<MaterialTextView>(R.id.trigger_count).text = resources.getQuantityString(
                R.plurals.achieved_times,
                trigger.value,
                trigger.value
            )

            val hidePercentage = trigger.type == TriggerJson.Type.NO_REASON
            item.findViewById<MaterialTextView>(R.id.trigger_percentage).apply {
                visibility = if (hidePercentage) View.GONE else View.VISIBLE
                if (!hidePercentage) text = LocalizationHelper.formatPercentage(value = trigger.percent)
            }
            item.findViewById<LinearProgressIndicator>(R.id.trigger_progress).apply {
                visibility = if (hidePercentage) View.GONE else View.VISIBLE
                if (!hidePercentage) setProgressCompat(trigger.percent.toInt().coerceIn(0, 100), true)
            }
            binding.triggerContainer.addView(item)
        }
    }
}