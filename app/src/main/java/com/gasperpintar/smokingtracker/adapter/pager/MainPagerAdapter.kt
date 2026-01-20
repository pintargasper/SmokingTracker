package com.gasperpintar.smokingtracker.adapter.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gasperpintar.smokingtracker.ui.fragment.AnalyticsFragment
import com.gasperpintar.smokingtracker.ui.fragment.GraphFragment
import com.gasperpintar.smokingtracker.ui.fragment.HomeFragment
import com.gasperpintar.smokingtracker.ui.fragment.SettingsFragment

class MainPagerAdapter(
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