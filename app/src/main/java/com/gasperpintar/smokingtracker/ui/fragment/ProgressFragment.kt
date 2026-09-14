package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Intent
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.CalculatorActivity
import com.gasperpintar.smokingtracker.NotesActivity
import com.gasperpintar.smokingtracker.StatisticsActivity
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