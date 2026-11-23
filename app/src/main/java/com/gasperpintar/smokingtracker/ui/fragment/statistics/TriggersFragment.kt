package com.gasperpintar.smokingtracker.ui.fragment.statistics

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

        state.triggers.forEach { trigger ->
            val item = layoutInflater.inflate(R.layout.item_statistics_trigger, binding.triggerContainer, false)
            val title = getString(TriggerJson.Title.valueOf(trigger.type.name).stringResource)
            val icon = TriggerJson.Icon.valueOf(trigger.type.name).drawableResource

            item.findViewById<MaterialTextView>(R.id.trigger_name).text = title
            item.findViewById<ShapeableImageView>(R.id.trigger_icon).setImageResource(icon)
            item.findViewById<MaterialTextView>(R.id.trigger_count).text = resources.getQuantityString(
                R.plurals.achievement_achieved_times,
                trigger.value,
                trigger.value
            )
            item.findViewById<MaterialTextView>(R.id.trigger_percentage).text = LocalizationHelper.formatPercentage(trigger.percent)

            binding.triggerContainer.addView(item)
        }
    }
}