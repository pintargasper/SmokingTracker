package com.gasperpintar.smokingtracker.ui.fragment

import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.databinding.FragmentMoreBinding
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementFragment
import com.gasperpintar.smokingtracker.ui.fragment.note.NotesFragment
import com.gasperpintar.smokingtracker.ui.fragment.statistics.StatisticsFragment

class MoreFragment : Base<FragmentMoreBinding>(
    bindingInflater = FragmentMoreBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        statisticsLayout.setOnClickListener {
            (requireActivity() as MainActivity).openSubFragment(fragment = StatisticsFragment())
        }

        calculatorLayout.setOnClickListener {
            (requireActivity() as MainActivity).openSubFragment(fragment = CalculatorFragment())
        }

        achievementsLayout.setOnClickListener {
            (requireActivity() as MainActivity).openSubFragment(fragment = AchievementFragment())
        }

        notesLayout.setOnClickListener {
            (requireActivity() as MainActivity).openSubFragment(fragment = NotesFragment())
        }
        showContent()
    }
}