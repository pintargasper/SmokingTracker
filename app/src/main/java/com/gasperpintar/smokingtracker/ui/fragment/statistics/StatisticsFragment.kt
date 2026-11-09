package com.gasperpintar.smokingtracker.ui.fragment.statistics

import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBinding
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import com.google.android.material.tabs.TabLayoutMediator

class StatisticsFragment : Base<FragmentStatisticsBinding>(
    bindingInflater = FragmentStatisticsBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupPager()
        showContent()
    }

    private fun setupPager() = binding.apply {
        statisticsViewPager.adapter = Pager(
            this@StatisticsFragment,
            listOf(
                { BasicFragment() },
                { ForecastFragment() }
            )
        )

        TabLayoutMediator(statisticsTabLayout, statisticsViewPager) { tab, position ->
            tab.text = getString(
                when (position) {
                    0 -> R.string.statistics_basic
                    1 -> R.string.statistics_forecast
                    else -> R.string.statistics_basic
                }
            )
        }.attach()
    }
}