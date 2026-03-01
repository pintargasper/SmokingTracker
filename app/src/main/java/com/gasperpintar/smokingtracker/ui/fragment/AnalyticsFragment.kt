package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gasperpintar.smokingtracker.AchievementsActivity
import com.gasperpintar.smokingtracker.CalculatorActivity
import com.gasperpintar.smokingtracker.StatisticsActivity
import com.gasperpintar.smokingtracker.databinding.FragmentAnalyticsBinding
import kotlin.reflect.KClass

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)

        setup()

        return binding.root
    }

    private fun setup() {
        binding.calculatorLayout.setOnClickListener {
            startActivity(CalculatorActivity::class)
        }

        binding.achievementsLayout.setOnClickListener {
            startActivity(AchievementsActivity::class)
        }

        binding.statisticsLayout.setOnClickListener {
            startActivity(StatisticsActivity::class)
        }
    }

    private fun startActivity(clazz: KClass<*>) {
        val intent = Intent(binding.root.context, clazz.java)
        binding.root.context.startActivity(intent)
    }
}