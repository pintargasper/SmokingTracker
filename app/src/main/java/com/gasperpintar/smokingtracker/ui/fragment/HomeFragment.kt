package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.databinding.HistoryContainerBinding
import com.gasperpintar.smokingtracker.di.ModelFactory
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatLocalized
import com.gasperpintar.smokingtracker.utils.TimeHelper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.milliseconds

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ModelFactory(container = (requireActivity().application as Application).container)
    }

    private var lastEntry: HistoryEntity? = null
    private var timerJob: Job? = null

    private lateinit var adapter: Adapter<HistoryEntry, HistoryContainerBinding>

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initialize()

        return binding.root
    }

    @Override
    override fun onResume() {
        super.onResume()
        startTimer()
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
        _binding = null
    }

    private fun initialize() = binding.apply {
        buttonAddEntry.setOnClickListener {
            DialogManager.showInsertDialog(context = requireActivity()) { isLent ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.insert(isLent = isLent)
                    loadHistory()
                }
            }
        }

        previousDay.setOnClickListener {
            viewModel.previousDay()
            loadHistory()
        }

        nextDay.setOnClickListener {
            viewModel.nextDay()
            loadHistory()
        }
        setupAdapter()
        loadHistory()
    }

    private fun setupAdapter() = binding.apply {
        adapter = Adapter(
            bindingFactory = HistoryContainerBinding::inflate,
            onBind = { historyEntry ->
                timerLabel.text = historyEntry.timerLabel
                lent.visibility = if (historyEntry.isLent) View.VISIBLE else View.GONE

                edit.setOnClickListener {
                    DialogManager.showEditDialog(context = requireActivity(), entry = historyEntry) { newDateTime, isLent ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.update(entry = historyEntry, dateTime = newDateTime, isLent = isLent)
                            loadHistory()
                        }
                    }
                }

                delete.setOnClickListener {
                    DialogManager.showDeleteDialog(context = requireActivity()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.delete(entry = historyEntry)
                            loadHistory()
                        }
                    }
                }
            }
        )
        recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
        recyclerviewHistory.adapter = adapter
    }

    private fun loadHistory() = binding.apply {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = viewModel.getHistory()
            lastEntry = state.lastEntry

            currentDay.text = LocalizationHelper.getDayOfWeekName(dayOfWeek = state.selectedDate.dayOfWeek)
            currentDate.text = state.selectedDate.formatLocalized()
            dailyValue.text = state.dailyCount.toString()
            weeklyValue.text = state.weeklyCount.toString()
            monthlyValue.text = state.monthlyCount.toString()

            updateTimerLabel(entry = lastEntry)

            adapter.submitList(state.history) {
                recyclerviewHistory.scrollToPosition(0)
            }
            WidgetHelper.updateAllWidgets(context = requireContext())
        }
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
        entry: HistoryEntity?
    ) = binding.apply {
        val duration = entry?.createdAt?.let { createdAt ->
            Duration.between(createdAt, LocalDateTime.now())
        }
        timerLabel.text = TimeHelper.formatDuration(resources = resources, duration = duration)
    }
}