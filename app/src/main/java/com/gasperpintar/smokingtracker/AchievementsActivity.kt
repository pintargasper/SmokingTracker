package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gasperpintar.smokingtracker.adapter.pager.AchievementsPagerAdapter
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.databinding.ActivityAchievementsBinding
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.google.android.material.tabs.TabLayoutMediator

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.achievementsViewPager
        val tabLayout = binding.achievementsTabLayout

        viewPager.adapter = AchievementsPagerAdapter(this)
        viewPager.isUserInputEnabled = true

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.achievements_smoke_free_time)
                1 -> getString(R.string.achievements_cigarettes_avoided)
                else -> getString(R.string.achievements_smoke_free_time)
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.getTabAt(position)?.select()
            }
        })

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(LocalizationHelper.getLocalizedContext(context = context, database = Provider.getDatabase(context.applicationContext)))
    }
}
