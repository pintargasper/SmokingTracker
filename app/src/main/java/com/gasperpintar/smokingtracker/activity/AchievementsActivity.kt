package com.gasperpintar.smokingtracker.activity

import android.os.Bundle
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.ActivityAchievementsBinding
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementsFragment
import com.google.android.material.tabs.TabLayoutMediator

class AchievementsActivity : Base<ActivityAchievementsBinding>(
    bindingInflater = ActivityAchievementsBinding::inflate
) {

    @Override
    override fun initialize() = binding.apply {
        buttonBack.setOnClickListener {
            finish()
        }
        setupPager()
    }

    private fun setupPager() = binding.apply {
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