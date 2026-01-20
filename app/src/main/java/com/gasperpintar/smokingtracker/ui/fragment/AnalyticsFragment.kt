package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.CalculatorActivity
import com.gasperpintar.smokingtracker.databinding.FragmentAnalyticsBinding

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setup()

        return root
    }

    private fun setup() {
        binding.calculatorLayout.setOnClickListener {
            val intent = Intent(binding.root.context, CalculatorActivity::class.java)
            binding.root.context.startActivity(intent)
        }

        binding.achievementsLayout.setOnClickListener {
            val intent = Intent(binding.root.context, AchievementsActivity::class.java)
            binding.root.context.startActivity(intent)
        }
    }
}