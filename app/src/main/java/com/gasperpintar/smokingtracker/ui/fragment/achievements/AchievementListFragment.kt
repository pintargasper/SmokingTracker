package com.gasperpintar.smokingtracker.ui.fragment.achievements

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.viewmodel.AchievementViewModel
import com.gasperpintar.smokingtracker.databinding.ContainerAchievementsBinding
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsListBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch

class AchievementListFragment @JvmOverloads constructor(
    private val initialAchievementType: AchievementCategory = AchievementCategory.CIGARETTES_AVOIDED
) : Base<FragmentAchievementsListBinding>(
    bindingInflater = FragmentAchievementsListBinding::inflate
) {

    private val viewModel: AchievementViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    private lateinit var achievementType: AchievementCategory
    private lateinit var adapter: Adapter<AchievementEntry, ContainerAchievementsBinding>

    @Override
    override fun initialize() {
        achievementType = AchievementCategory.valueOf(
            arguments?.getString(ARG_ACHIEVEMENT_TYPE, initialAchievementType.name) ?: initialAchievementType.name
        )

        setupAdapter()

        viewLifecycleOwner.lifecycleScope.launch {
            loadAchievements()
            showContent()
        }
    }

    private fun setupAdapter() = binding.apply {
        adapter = ContainerManager.createAchievementAdapter(context = requireActivity())
        recyclerviewAchievements.layoutManager = LinearLayoutManager(requireContext())
        recyclerviewAchievements.adapter = adapter
    }

    private suspend fun loadAchievements() {
        val state = viewModel.getState(category = achievementType)
        if (!isAdded || view == null) return
        adapter.submitList(state.achievements) {
            if (state.achievements.isNotEmpty()) binding.recyclerviewAchievements.scrollToPosition(0)
        }
    }

    companion object {
        private const val ARG_ACHIEVEMENT_TYPE = "achievement_type"

        @JvmStatic
        fun newInstance(
            achievementType: AchievementCategory
        ) = AchievementListFragment(initialAchievementType = achievementType).apply {
            arguments = android.os.Bundle().apply {
                putString(ARG_ACHIEVEMENT_TYPE, achievementType.name)
            }
        }
    }
}