package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gasperpintar.smokingtracker.databinding.ActivityStatisticsBinding
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.fragment.statistics.BasicFragment
import com.gasperpintar.smokingtracker.ui.fragment.statistics.ForecastFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.google.android.material.tabs.TabLayoutMediator

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)

        initialize()

        setContentView(binding.root)
    }

    @Override
    override fun attachBaseContext(
        context: Context
    ) {
        super.attachBaseContext(
            LocalizationHelper.getLocalizedContext(
                context = context,
                settingsRepository = (context.applicationContext as Application).container.settingsRepository
            )
        )
    }

    private fun initialize() = with(receiver = binding) {
        buttonBack.setOnClickListener {
            finish()
        }
        setupPager()
    }

    private fun setupPager() = with(receiver = binding) {
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
