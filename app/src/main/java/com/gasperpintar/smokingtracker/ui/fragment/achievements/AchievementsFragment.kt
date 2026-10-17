package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.viewmodel.AchievementViewModel
import com.gasperpintar.smokingtracker.databinding.ContainerAchievementsBinding
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import kotlinx.coroutines.launch

class AchievementsFragment : Base<FragmentAchievementsBinding>(
    bindingInflater = FragmentAchievementsBinding::inflate
) {

    private val viewModel: AchievementViewModel by viewModels {
        ModelFactory(
            container = (requireActivity().application as Application).container
        )
    }

    private lateinit var achievementType: AchievementCategory
    private lateinit var adapter: Adapter<AchievementEntry, ContainerAchievementsBinding>

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        achievementType = AchievementCategory.valueOf(
            requireArguments().getString("achievement_type")!!
        )
    }

    @Override
    override fun initialize() {
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

    private suspend fun loadAchievements() = binding.apply {
        val state = viewModel.getState(category = achievementType)
        adapter.submitList(state.achievements) {
            recyclerviewAchievements.scrollToPosition(0)
        }
    }
}