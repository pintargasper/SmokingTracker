package com.gasperpintar.smokingtracker.activity

import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.ActivityStatisticsBinding
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.fragment.statistics.BasicFragment
import com.gasperpintar.smokingtracker.ui.fragment.statistics.ForecastFragment
import com.google.android.material.tabs.TabLayoutMediator

class StatisticsActivity : Base<ActivityStatisticsBinding>(
    bindingInflater = ActivityStatisticsBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            finish()
        }
        setupPager()
    }

    private fun setupPager() = binding.apply {
        statisticsViewPager.adapter = Pager(
            this@StatisticsActivity,
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
