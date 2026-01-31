package com.gasperpintar.smokingtracker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class Pager(
    fragmentActivity: FragmentActivity,
    private val fragmentCreator: List<() -> Fragment>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return fragmentCreator.size
    }

    override fun createFragment(
        position: Int
    ): Fragment {
        return fragmentCreator.getOrElse(index = position) { fragmentCreator.first() }()
    }
}
