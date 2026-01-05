package com.gasperpintar.smokingtracker.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.adapter.history.HistoryAdapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.ui.DialogManager
import com.gasperpintar.smokingtracker.utils.Helper
import com.gasperpintar.smokingtracker.utils.Helper.toHistoryEntry
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: Lazy<AppDatabase>
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var selectedDate: LocalDate
    private var lastEntry: HistoryEntity? = null
    private var timeDifference: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        database = lazy { (requireActivity() as MainActivity).database }
        selectedDate = LocalDate.now()

        setup()
        refreshUI()
        timeDifference()
        setupRecyclerView()

        return root
    }

    private fun setup() {
        binding.buttonAddEntry.setOnClickListener {
            DialogManager.showInsertDialog(
                context = requireActivity(),
                layoutInflater = layoutInflater,
                database = database.value,
                lifecycleScope = lifecycleScope,
                refreshUI = { refreshUI() }
            )
        }

        binding.previousDay.setOnClickListener {
            selectedDate = selectedDate.minusDays(1)
            refreshUI()
        }

        binding.nextDay.setOnClickListener {
            selectedDate = selectedDate.plusDays(1)
            refreshUI()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onEditClick = { entry ->
                DialogManager.showEditDialog(
                    context = requireActivity(),
                    layoutInflater = layoutInflater,
                    database = database.value,
                    lifecycleScope = lifecycleScope,
                    entry = entry,
                    refreshUI = { refreshUI() }
                )
            },
            onDeleteClick = { entry ->
                DialogManager.showDeleteDialog(
                    context = requireActivity(),
                    layoutInflater = layoutInflater,
                    database = database.value,
                    lifecycleScope = lifecycleScope,
                    entry = entry,
                    refreshUI = { refreshUI() }
                )
            }
        )
        binding.recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewHistory.adapter = historyAdapter
    }

    @SuppressLint("DefaultLocale")
    private fun refreshUI() {
        binding.currentDate.text = LocalizationHelper.formatDate(selectedDate)
        lifecycleScope.launch {
            updateStatistics(selectedDate)
            loadHistoryForDay(selectedDate)
        }
        WidgetHelper.updateWidget(context = requireContext())
        updateLastEntry()
    }

    private fun updateLastEntry() {
        lifecycleScope.launch {
            lastEntry = database.value.historyDao()
                .getLastHistoryEntry(endOfToday = Helper.getEndOfDay(date = LocalDate.now()))
            updateTimerLabel()
        }
    }

    private fun timeDifference() {
        timeDifference?.cancel()
        timeDifference = lifecycleScope.launch {
            while (isActive) {
                updateTimerLabel()
                delay(timeMillis = 1000)
            }
        }
    }

    private fun stopTimeDifference() {
        timeDifference?.cancel()
        timeDifference = null
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerLabel() {
        val entry = lastEntry

        val timeDifference: String = if (entry?.createdAt != null) {
            val duration: Duration = Duration.between(entry.createdAt, LocalDateTime.now())
            String.format(
                "%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutes() % 60,
                duration.seconds % 60
            )
        } else "00:00:00"
        binding.timerLabel.text = timeDifference
    }

    private suspend fun updateStatistics(date: LocalDate) {
        val (startOfDay, endOfDay) = Helper.getDay(date = date)
        val dailyCount = database.value.historyDao().getHistoryCountBetween(start = startOfDay, end = endOfDay)

        val (startOfWeek, endOfWeek) = Helper.getWeek(date = date)
        val weeklyCount = database.value.historyDao().getHistoryCountBetween(start = startOfWeek, end = endOfWeek)

        val (startOfMonth, endOfMonth) = Helper.getMonth(date = date)
        val monthlyCount = database.value.historyDao().getHistoryCountBetween(start = startOfMonth, end = endOfMonth)

        binding.dailyValue.text = dailyCount.toString()
        binding.weeklyValue.text = weeklyCount.toString()
        binding.monthlyValue.text = monthlyCount.toString()
    }

    private suspend fun loadHistoryForDay(date: LocalDate) {
        val (startOfDay, endOfDay) = Helper.getDay(date = date)
        val entityList = database.value.historyDao().getHistoryBetween(start = startOfDay, end = endOfDay)
        val historyList = entityList.map { it.toHistoryEntry() }
        historyAdapter.submitList(historyList) {
            binding.recyclerviewHistory.scrollToPosition(0)
        }
    }

    override fun onResume() {
        super.onResume()
        timeDifference()
    }

    override fun onPause() {
        super.onPause()
        stopTimeDifference()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimeDifference()
        _binding = null
    }
}
