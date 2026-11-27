package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.viewmodel.AchievementViewModel
import com.gasperpintar.smokingtracker.databinding.ContainerAchievementsBinding
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsListBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.model.AchievementJson
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch

class AchievementListFragment @JvmOverloads constructor(
    private val initialType: AchievementJson.Category = AchievementJson.Category.CIGARETTES_AVOIDED
) : Base<FragmentAchievementsListBinding>(
    bindingInflater = FragmentAchievementsListBinding::inflate
) {

    private val viewModel: AchievementViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    private lateinit var achievementType: AchievementJson.Category
    private lateinit var adapter: Adapter<AchievementEntry, ContainerAchievementsBinding>

    @Override
    override fun initialize() {
        achievementType = AchievementJson.Category.valueOf(
            arguments?.getString(ACHIEVEMENT_TYPE, initialType.name) ?: initialType.name
        )

        setupAdapter()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                loadAchievements()
                showContent()
            }
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
        adapter.submitList(state.achievements)
    }

    companion object {
        private const val ACHIEVEMENT_TYPE = "achievement_type"

        @JvmStatic
        fun newInstance(
            achievementType: AchievementJson.Category
        ): AchievementListFragment {
            return AchievementListFragment(initialType = achievementType).apply {
                arguments = Bundle().apply {
                    putString(ACHIEVEMENT_TYPE, achievementType.name)
                }
            }
        }
    }
}