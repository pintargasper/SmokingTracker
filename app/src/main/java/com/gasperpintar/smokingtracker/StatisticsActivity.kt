package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gasperpintar.smokingtracker.adapter.Pager
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.databinding.ActivityStatisticsBinding
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.MarqueeTextView
import com.gasperpintar.smokingtracker.ui.fragment.statistics.StatisticsFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.google.android.material.tabs.TabLayoutMediator

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding

    lateinit var database: AppDatabase
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createPager()

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun attachBaseContext(
        context: Context
    ) {
        database = Provider.getDatabase(context = context.applicationContext)
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        super.attachBaseContext(LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository))
    }

    private fun createPager() {
        val viewPager = binding.statisticsViewPager
        val tabLayout = binding.statisticsTabLayout

        val fragments = listOf(
            { StatisticsFragment.newInstance(type = 0) },
            { StatisticsFragment.newInstance(type = 1) }
        )

        val tabTitles = listOf(
            getString(R.string.statistics_basic),
            getString(R.string.statistics_forecast)
        )

        viewPager.adapter = Pager(
            fragmentActivity = this,
            fragmentCreator = fragments
        )
        viewPager.isUserInputEnabled = true

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customTabView = layoutInflater.inflate(
                R.layout.view_tab,
                tabLayout,
                false
            ) as MarqueeTextView

            customTabView.text = tabTitles.getOrNull(position) ?: ""
            customTabView.isSelected = true
            tab.customView = customTabView
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.getTabAt(position)?.select()
            }
        })
    }
}
