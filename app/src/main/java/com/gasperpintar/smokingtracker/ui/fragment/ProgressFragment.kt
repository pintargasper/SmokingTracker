package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Intent
import com.gasperpintar.smokingtracker.activity.AchievementsActivity
import com.gasperpintar.smokingtracker.activity.CalculatorActivity
import com.gasperpintar.smokingtracker.activity.NotesActivity
import com.gasperpintar.smokingtracker.activity.StatisticsActivity
import com.gasperpintar.smokingtracker.databinding.FragmentProgressBinding

class ProgressFragment : Base<FragmentProgressBinding>(
    bindingInflater = FragmentProgressBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        statisticsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), StatisticsActivity::class.java))
        }

        calculatorLayout.setOnClickListener {
            startActivity(Intent(requireContext(), CalculatorActivity::class.java))
        }

        achievementsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), AchievementsActivity::class.java))
        }

        notesLayout.setOnClickListener {
            startActivity(Intent(requireContext(), NotesActivity::class.java))
        }
        showContent()
    }
}