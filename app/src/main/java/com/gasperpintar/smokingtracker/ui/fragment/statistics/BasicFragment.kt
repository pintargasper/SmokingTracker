package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
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
            statistic(title = R.string.statistics_max_cigarettes, value = state.maxCigarettes.toString(),
                caption = LocalizationHelper.formatLoggedDate(resources, state.maxCigarettesDate)),
            statistic(title = R.string.statistics_min_cigarettes, value = state.minCigarettes.toString(),
                caption = LocalizationHelper.formatLoggedDate(resources, state.minCigarettesDate)),
            statistic(title = R.string.statistics_avg_cigarettes, value = "%.2f".format(Locale.getDefault(), state.averageCigarettes)),
            statistic(title = R.string.statistics_total_cigarettes, value = state.totalCigarettes.toString()),
            statistic(title = R.string.statistics_first_entry, value = state.firstRecordDate?.let {
                TimeHelper.getDurationString(resources, start = it)
            } ?: fallback),
            statistic(title = R.string.statistics_longest_streak, value = state.longestStreak?.let { (start, end) ->
                TimeHelper.getDurationString(resources, start, end)
            } ?: fallback)
        )

        val money = listOf(
            statistic(title = R.string.statistics_total_spent, value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.totalSpent)),
            statistic(title = R.string.statistics_total_spent_today, value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.todaySpent)),
            statistic(title = R.string.statistics_total_spent_month, value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.monthSpent)),
            statistic(title = R.string.statistics_most_expensive, value = LocalizationHelper.formatMoney(settingsRepository = settings, value = state.mostExpensiveDaySpent),
                caption = LocalizationHelper.formatLoggedDate(resources, state.mostExpensiveDay))
        )

        statisticsTitleInstructions.isVisible = !state.hasCosts

        setupPager(cigarettesViewPager, cigarettesPageIndicator, pages = cigarettes)
        setupPager(moneyViewPager, moneyPageIndicator, pages = money)
        setupNestedPagerTouch(pager = cigarettesViewPager)
        setupNestedPagerTouch(pager = moneyViewPager)
    }

    private fun setupPager(
        pager: ViewPager2,
        indicator: TextView,
        pages: List<Triple<String, String, String?>>
    ) {
        pager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            override fun onCreateViewHolder(parent: ViewGroup, type: Int) =
                object : RecyclerView.ViewHolder(ItemStatisticsPageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ).root.apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }) {}

            @Override
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val (title, value, caption) = pages[position]
                ItemStatisticsPageBinding.bind(holder.itemView).run {
                    pageTitle.text = title
                    pageValue.text = value
                    pageCaption.text = caption.orEmpty()
                    pageCaption.isVisible = !caption.isNullOrBlank()
                }
            }

            @Override
            override fun getItemCount(): Int {
                return pages.size
            }
        }

        fun updateIndicator(position: Int) {
            indicator.text = "%d / %d".format(position + 1, pages.size)
        }

        updateIndicator(0)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @Override
            override fun onPageSelected(position: Int) {
                updateIndicator(position)
            }
        })
    }

    private fun setupNestedPagerTouch(pager: ViewPager2) {
        val recyclerView = pager.getChildAt(0) as? RecyclerView ?: return
        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            @Override
            override fun onInterceptTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent): Boolean {
                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> (parentFragment as? StatisticsFragment)?.setOuterPagerSwipeEnabled(false)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> (parentFragment as? StatisticsFragment)?.setOuterPagerSwipeEnabled(true)
                }
                return false
            }
        })
    }

    private fun statistic(title: Int, value: String, caption: String? = null): Triple<String, String, String?> {
        return Triple(getString(title), value, caption)
    }
}