package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.achievements.AchievementsAdapter
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.gasperpintar.smokingtracker.ui.DialogManager
import java.time.LocalDateTime

class AchievementsFragment(private val achievementType: AchievementCategory) : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private lateinit var achievementsAdapter: AchievementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        loadAchievements(achievementEntries = getAchievementsForType(type = achievementType))

        return root
    }

    private fun getAchievementsForType(type: AchievementCategory): List<AchievementEntry> {
        return when (type) {
            AchievementCategory.SMOKE_FREE_TIME -> listOf(
                AchievementEntry(0, R.drawable.ic_edit, 1, "", LocalDateTime.now(), AchievementCategory.SMOKE_FREE_TIME, AchievementUnit.HOURS),
                AchievementEntry(0, R.drawable.ic_edit, 5, "", LocalDateTime.now(), AchievementCategory.SMOKE_FREE_TIME, AchievementUnit.HOURS),
                AchievementEntry(0, R.drawable.ic_edit, 2, "", LocalDateTime.now(), AchievementCategory.SMOKE_FREE_TIME, AchievementUnit.DAYS),
                AchievementEntry(0, R.drawable.ic_edit, 1, "", LocalDateTime.now(), AchievementCategory.SMOKE_FREE_TIME, AchievementUnit.WEEKS),
            )

            AchievementCategory.CIGARETTES_AVOIDED -> listOf(
                AchievementEntry(0, R.drawable.ic_edit, 10, "", LocalDateTime.now(), AchievementCategory.CIGARETTES_AVOIDED, AchievementUnit.HOURS),
                AchievementEntry(0, R.drawable.ic_edit, 50, "", LocalDateTime.now(), AchievementCategory.CIGARETTES_AVOIDED, AchievementUnit.HOURS),
                AchievementEntry(0, R.drawable.ic_edit, 200, "", LocalDateTime.now(), AchievementCategory.CIGARETTES_AVOIDED, AchievementUnit.DAYS),
                AchievementEntry(0, R.drawable.ic_edit, 1000, "", LocalDateTime.now(), AchievementCategory.CIGARETTES_AVOIDED, AchievementUnit.WEEKS),
            )
        }
    }

    private fun setupRecyclerView() {
        achievementsAdapter = AchievementsAdapter(
            onItemClick = { achievementEntry ->
                DialogManager.showAchievementsDialog(
                    context = requireActivity(),
                    layoutInflater = layoutInflater,
                    entry = achievementEntry
                )
            }
        )
        binding.recyclerviewAchievements.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerviewAchievements.adapter = achievementsAdapter
    }

    private fun loadAchievements(achievementEntries: List<AchievementEntry>) {
        achievementsAdapter.submitList(achievementEntries) {
            binding.recyclerviewAchievements.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}