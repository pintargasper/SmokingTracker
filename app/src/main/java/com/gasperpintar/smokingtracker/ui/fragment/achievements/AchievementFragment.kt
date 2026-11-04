package com.gasperpintar.smokingtracker.ui.fragment.achievements

import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.databinding.FragmentAchievementsBinding
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.adapter.Pager
import com.gasperpintar.smokingtracker.ui.fragment.Base
import com.google.android.material.tabs.TabLayoutMediator

class AchievementFragment : Base<FragmentAchievementsBinding>(
    bindingInflater = FragmentAchievementsBinding::inflate
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
        achievementsViewPager.adapter = Pager(
            this@AchievementFragment,
            creators = listOf(
                { AchievementListFragment.newInstance(achievementType = AchievementCategory.SMOKE_FREE_TIME) },
                { AchievementListFragment.newInstance(achievementType = AchievementCategory.CIGARETTES_AVOIDED) }
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
}