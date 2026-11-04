package com.gasperpintar.smokingtracker.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class Pager : FragmentStateAdapter {

    private val fragmentCreators: List<() -> Fragment>

    constructor(activity: FragmentActivity, creators: List<() -> Fragment>) : super(activity) {
        fragmentCreators = creators
    }

    constructor(fragment: Fragment, creators: List<() -> Fragment>) : super(fragment) {
        fragmentCreators = creators
    }

    @Override
    override fun getItemCount(): Int {
        return fragmentCreators.size
    }

    @Override
    override fun createFragment(position: Int): Fragment {
        return fragmentCreators[position].invoke()
    }
}