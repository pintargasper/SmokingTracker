package com.gasperpintar.smokingtracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.SmokingTrackerApp
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.di.AppModelFactory
import com.gasperpintar.smokingtracker.ui.dialog.DialogManager
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
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
        AppModelFactory(appContainer = (requireActivity().application as SmokingTrackerApp).appContainer)
    }

    private var timerJob: Job? = null

    private lateinit var historyAdapter: Adapter<HistoryEntry>

    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentHomeBinding.inflate(inflater, container,false)

        initialize()
        setupAdapter()
        observeState()

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

    private fun initialize() {
        binding.buttonAddEntry.setOnClickListener {
            DialogManager.showInsertDialog(context = requireActivity()) { isLent ->
                viewModel.insert(isLent = isLent)
            }
        }

        binding.previousDay.setOnClickListener {
            val date = viewModel.uiState.value.selectedDate
            viewModel.selectDate(date = date.minusDays(1))
        }

        binding.nextDay.setOnClickListener {
            val date = viewModel.uiState.value.selectedDate
            viewModel.selectDate(date = date.plusDays(1))
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.currentDay.text = LocalizationHelper.getDayOfWeekName(
                    context = requireContext(),
                    dayOfWeek = state.selectedDate.dayOfWeek
                )

                binding.currentDate.text = LocalizationHelper.formatDate(state.selectedDate)
                binding.dailyValue.text = state.dailyCount.toString()
                binding.weeklyValue.text = state.weeklyCount.toString()
                binding.monthlyValue.text = state.monthlyCount.toString()

                historyAdapter.submitList(state.history) {
                    binding.recyclerviewHistory.scrollToPosition(0)
                }
                updateTimerLabel(entry = state.lastEntry)
            }
        }
    }

    private fun setupAdapter() {
        historyAdapter = Adapter(
            layoutId = R.layout.history_container,
            onBind = { itemView, historyEntry ->
                val timerLabel = itemView.findViewById<TextView>(R.id.timer_label)
                val lentButton = itemView.findViewById<ImageButton>(R.id.lent)
                val editButton = itemView.findViewById<ImageButton>(R.id.image_button_edit)
                val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)

                timerLabel.text = historyEntry.timerLabel
                lentButton.visibility = if (historyEntry.isLent) View.VISIBLE else View.GONE

                editButton.setOnClickListener {
                    DialogManager.showEditDialog(context = requireActivity(), entry = historyEntry) { newDateTime, isLent ->
                        viewModel.update(entry = historyEntry, dateTime = newDateTime, isLent = isLent)
                    }
                }

                deleteButton.setOnClickListener {
                    DialogManager.showDeleteDialog(context = requireActivity()) {
                        viewModel.delete(entry = historyEntry)
                    }
                }
            }
        )
        binding.recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewHistory.adapter = historyAdapter
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (isActive) {
                updateTimerLabel(entry = viewModel.uiState.value.lastEntry)
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
    ) {
        val duration = entry?.createdAt?.let { createdAt ->
            Duration.between(createdAt, LocalDateTime.now())
        }
        binding.timerLabel.text = TimeHelper.formatDuration(resources = resources, duration = duration)
    }
}