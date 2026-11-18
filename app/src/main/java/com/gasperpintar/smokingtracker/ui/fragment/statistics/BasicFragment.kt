package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.viewmodel.BasicViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBasicBinding
import com.gasperpintar.smokingtracker.databinding.ItemStatisticsPageBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.adapter.PageAdapter
import com.gasperpintar.smokingtracker.ui.fragment.Base
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.launch
import java.util.Locale

class BasicFragment : Base<FragmentStatisticsBasicBinding>(
    bindingInflater = FragmentStatisticsBasicBinding::inflate
) {

    private val viewModel: BasicViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    @Override
    override fun initialize() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadStatistics()
            showContent()
        }
    }

    @Override
    override fun onDestroyView() {
        (parentFragment as? StatisticsFragment)?.setOuterPagerSwipeEnabled(true)
        super.onDestroyView()
    }

    private suspend fun loadStatistics() = binding.apply {
        val container = (requireActivity().application as Application).container
        val settings = container.settingsRepository
        val state = viewModel.getState()
        val fallback = resources.getQuantityString(R.plurals.time_minutes, 0, 0)

        val cigarettes = listOf(
            PageAdapter.Page(
                title = getString(R.string.statistics_max_cigarettes),
                value = state.maxCigarettes.toString(),
                caption = LocalizationHelper.formatLoggedDate(resources, state.maxCigarettesDate)
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_min_cigarettes),
                value = state.minCigarettes.toString(),
                caption = LocalizationHelper.formatLoggedDate(resources, state.minCigarettesDate)
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_avg_cigarettes),
                value = "%.2f".format(Locale.getDefault(), state.averageCigarettes)
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_total_cigarettes),
                value = state.totalCigarettes.toString()
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_first_entry),
                value = state.firstRecordDate?.let {
                    TimeHelper.getDurationString(resources, start = it)
                } ?: fallback
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_longest_streak),
                value = state.longestStreak?.let { (start, end) ->
                    TimeHelper.getDurationString(resources, start, end)
                } ?: fallback
            )
        )

        val money = listOf(
            PageAdapter.Page(
                title = getString(R.string.statistics_total_spent),
                value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.totalSpent)
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_total_spent_today),
                value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.todaySpent)
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_total_spent_month),
                value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.monthSpent)
            ),
            PageAdapter.Page(
                title = getString(R.string.statistics_most_expensive),
                value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.mostExpensiveDaySpent),
                caption = LocalizationHelper.formatLoggedDate(resources, state.mostExpensiveDay)
            )
        )
        statisticsTitleInstructions.isVisible = !state.hasCosts

        PageAdapter.setupPager(pager = cigarettesViewPager, indicator = cigarettesPageIndicator, pages = cigarettes, bindingInflater = ItemStatisticsPageBinding::inflate)
        PageAdapter.setupPager(pager = moneyViewPager, indicator = moneyPageIndicator, pages = money, bindingInflater = ItemStatisticsPageBinding::inflate)
        setupNestedPagerTouch(cigarettesViewPager)
        setupNestedPagerTouch(moneyViewPager)
    }

    private fun setupNestedPagerTouch(pager: ViewPager2) {
        val recyclerView = pager.getChildAt(0) as? RecyclerView ?: return
        recyclerView.addOnItemTouchListener(
            object : RecyclerView.SimpleOnItemTouchListener() {
                @Override
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView,
                    motionEvent: MotionEvent
                ): Boolean {
                    when (motionEvent.actionMasked) {
                        MotionEvent.ACTION_DOWN -> (parentFragment as? StatisticsFragment)?.setOuterPagerSwipeEnabled(false)
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> (parentFragment as? StatisticsFragment)?.setOuterPagerSwipeEnabled(true)
                    }
                    return false
                }
            }
        )
    }
}