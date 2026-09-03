package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.CalculatorActivity
import com.gasperpintar.smokingtracker.NotesActivity
import com.gasperpintar.smokingtracker.StatisticsActivity
import com.gasperpintar.smokingtracker.databinding.FragmentProgressBinding

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentProgressBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initialize() {
        binding.statisticsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), StatisticsActivity::class.java))
        }

        binding.calculatorLayout.setOnClickListener {
            startActivity(Intent(requireContext(), CalculatorActivity::class.java))
        }

        binding.achievementsLayout.setOnClickListener {
            startActivity(Intent(requireContext(), AchievementsActivity::class.java))
        }

        binding.notesLayout.setOnClickListener {
            startActivity(Intent(requireContext(), NotesActivity::class.java))
        }
    }
}