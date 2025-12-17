package com.gasperpintar.smokingtracker.adapter.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gasperpintar.smokingtracker.ui.analytics.AnalyticsFragment
import com.gasperpintar.smokingtracker.ui.graph.GraphFragment
import com.gasperpintar.smokingtracker.ui.home.HomeFragment
import com.gasperpintar.smokingtracker.ui.settings.SettingsFragment

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> GraphFragment()
            2 -> AnalyticsFragment()
            3 -> SettingsFragment()
            else -> HomeFragment()
        }
    }
}
