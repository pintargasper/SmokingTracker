package com.gasperpintar.smokingtracker

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.databinding.ActivityAchievementsBinding
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementsFragment
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.google.android.material.tabs.TabLayoutMediator

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    @Override
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)

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
        achievementsViewPager.adapter = Pager(
            this@AchievementsActivity,
            listOf(
                { createAchievementsFragment(AchievementCategory.SMOKE_FREE_TIME) },
                { createAchievementsFragment(AchievementCategory.CIGARETTES_AVOIDED) }
            )
        )

        TabLayoutMediator(achievementsTabLayout, achievementsViewPager) { tab, position ->
            tab.text = getString(
                when (position) {
                    0 -> R.string.achievements_time
                    1 -> R.string.achievements_avoided
                    else -> R.string.achievements_time
                }
            )
        }.attach()
    }

    private fun createAchievementsFragment(
        achievementType: AchievementCategory
    ) = AchievementsFragment().apply {
        arguments = Bundle().apply {
            putString("achievement_type", achievementType.name)
        }
    }
}