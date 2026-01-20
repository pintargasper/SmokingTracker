package com.gasperpintar.smokingtracker.adapter.pager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementsFragment

class AchievementsPagerAdapter(
    activity: AppCompatActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AchievementsFragment(achievementType = AchievementCategory.SMOKE_FREE_TIME)
            1 -> AchievementsFragment(achievementType = AchievementCategory.CIGARETTES_AVOIDED)
            else -> AchievementsFragment(achievementType = AchievementCategory.SMOKE_FREE_TIME)
        }
    }
}
