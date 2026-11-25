package com.gasperpintar.smokingtracker.ui.fragment

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.databinding.ContainerContentHistoryBinding
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

class HomeFragment : Base<FragmentHomeBinding>(
    bindingInflater = FragmentHomeBinding::inflate
) {

    private val viewModel: HomeViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    private var showIntervalLabel: Boolean = false
    private var historyEntries: List<HistoryEntry> = emptyList()
    private var lastEntry: HistoryEntry? = null
    private var timerJob: Job? = null

    private lateinit var adapter: Adapter<HistoryEntry, ContainerContentHistoryBinding>

    @Override
    override fun initialize() = binding.apply {
        buttonAddEntry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                DialogManager.showInsertDialog(context = requireActivity(), defaultTrigger = viewModel.getState().defaultTrigger) { isLent, trigger ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.insert(isLent = isLent, trigger = trigger)
                        loadHistory()
                    }
                }
            }
        }

        previousDay.setOnClickListener {
            viewModel.previousDay()
            viewLifecycleOwner.lifecycleScope.launch {
                loadHistory()
            }
        }

        nextDay.setOnClickListener {
            viewModel.nextDay()
            viewLifecycleOwner.lifecycleScope.launch {
                loadHistory()
            }
        }

        binding.cardDatePicker.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val date = viewModel.getState().selectedDate
                DialogManager.showDatePickerDialog(context = requireActivity(), initialDate = TimeHelper.fromLocalDate(date)) { date ->
                    viewModel.selectDate(TimeHelper.toLocalDate(calendar = date))
                    viewLifecycleOwner.lifecycleScope.launch {
                        loadHistory()
                    }
                }
            }
        }

        setupAdapter()

        viewLifecycleOwner.lifecycleScope.launch {
            loadHistory()
            showContent()
        }
    }

    @Override
    override fun onResume() {
        super.onResume()
        startTimer()
        viewLifecycleOwner.lifecycleScope.launch {
            loadHistory(updateWidgets = false)
        }
    }

    @Override
    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    @Override
    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
    }

    private fun setupAdapter() = binding.apply {
        adapter = ContainerManager.createHistoryAdapter(
            context = requireActivity(),
            entries = { historyEntries },
            showIntervalLabels = { showIntervalLabel },
            onEdit = { entry ->
                DialogManager.showEditDialog(context = requireActivity(), entry = entry) { newDateTime, isLent, trigger ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.update(entry, newDateTime, isLent, trigger)
                        loadHistory()
                    }
                }
            },
            onDelete = { entry ->
                DialogManager.showDeleteDialog(context = requireActivity()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete(entry)
                        loadHistory()
                    }
                }
            }
        )
        recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
        recyclerviewHistory.adapter = adapter
    }

    private suspend fun loadHistory(updateWidgets: Boolean = true) = binding.apply {
        val state = viewModel.getState()
        showIntervalLabel = state.showIntervalLabel
        historyEntries = state.history
        lastEntry = state.lastEntry

        currentDay.text = LocalizationHelper.getDayOfWeekName(dayOfWeek = state.selectedDate.dayOfWeek)
        currentDate.text = LocalizationHelper.formatDate(date = state.selectedDate)
        dailyValue.text = state.dailyCount.toString()
        weeklyValue.text = state.weeklyCount.toString()
        monthlyValue.text = state.monthlyCount.toString()

        updateTimerLabel(entry = lastEntry)

        state.history.let {
            recyclerviewHistory.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            layoutEmptyHistory.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE

            adapter.submitList(it) {
                adapter.notifyItemRangeChanged(0, it.size)
                recyclerviewHistory.scrollToPosition(0)
            }
        }
        if (updateWidgets) WidgetHelper.updateAllWidgets(context = requireContext())
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateTimerLabel(lastEntry)
                delay(duration = 1000.milliseconds)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateTimerLabel(
        entry: HistoryEntry?
    ) = binding.apply {
        val duration = entry?.createdAt?.let { createdAt ->
            Duration.between(createdAt, LocalDateTime.now())
        }
        timerLabel.text = TimeHelper.formatDuration(resources = resources, duration = duration)
    }
}