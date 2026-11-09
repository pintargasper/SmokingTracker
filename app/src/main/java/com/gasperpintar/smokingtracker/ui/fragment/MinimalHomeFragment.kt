package com.gasperpintar.smokingtracker.ui.fragment

import android.content.res.ColorStateList
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentMinimalHomeBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning
import com.gasperpintar.smokingtracker.utils.TimeHelper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

class MinimalHomeFragment : Base<FragmentMinimalHomeBinding>(
    bindingInflater = FragmentMinimalHomeBinding::inflate
) {

    private val viewModel: HomeViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    private var lastEntry: HistoryEntity? = null
    private var dayEndMinutes = 0
    private var currentDay: LocalDate? = null
    private var refreshJob: Job? = null
    private var warningMinutes = ElapsedTimeWarning.DEFAULT_MINUTES
    private lateinit var defaultTimerTextColors: ColorStateList

    @Override
    override fun initialize() {
        defaultTimerTextColors = binding.timerLabel.textColors
        binding.buttonAddCigarette.setOnClickListener { viewModel.insertNow() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                launch { viewModel.historyChanges.collect { refreshHome() } }
                launch {
                    viewModel.isAdding.collect { isAdding ->
                        binding.buttonAddCigarette.isEnabled = !isAdding
                    }
                }
                launch {
                    viewModel.addEvents.collect { success ->
                        if (success) {
                            WidgetHelper.updateAllWidgets(context = requireContext())
                            Snackbar.make(binding.root, R.string.home_add_success, Snackbar.LENGTH_SHORT).show()
                        } else {
                            Snackbar.make(binding.root, R.string.home_add_error, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                while (isActive) {
                    val now = LocalDateTime.now()
                    if (currentDay != null && currentDay != TimeHelper.dayDate(now, dayEndMinutes)) {
                        refreshHome()
                    }
                    updateElapsedTime(now)
                    delay(duration = 1_000.milliseconds)
                }
            }
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        refreshHome()
    }

    private fun refreshHome() {
        if (refreshJob?.isActive == true || view == null) return
        refreshJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val state = viewModel.getMinimalState()
                lastEntry = state.lastEntry
                dayEndMinutes = state.dayEndMinutes
                warningMinutes = state.elapsedTimeWarningMinutes
                currentDay = TimeHelper.dayDate(LocalDateTime.now(), dayEndMinutes)
                binding.todayCount.text = state.smokedToday.toString()
                updateElapsedTime(LocalDateTime.now())
                showContent()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (view != null) {
                    Snackbar.make(binding.root, R.string.home_load_error, Snackbar.LENGTH_LONG).show()
                }
            } finally {
                refreshJob = null
            }
        }
    }

    private fun updateElapsedTime(now: LocalDateTime) {
        val duration = lastEntry?.createdAt?.let { createdAt -> Duration.between(createdAt, now) }
        binding.timerLabel.text = if (lastEntry == null) {
            getString(R.string.home_no_smoked_entries)
        } else {
            TimeHelper.formatDuration(resources = resources, duration = duration)
        }
        val showWarning = duration?.let { ElapsedTimeWarning.shouldHighlight(it, warningMinutes) } == true
        if (showWarning) {
            binding.timerLabel.setTextColor(
                MaterialColors.getColor(binding.timerLabel, androidx.appcompat.R.attr.colorError)
            )
        } else {
            binding.timerLabel.setTextColor(defaultTimerTextColors)
        }
    }
}
