package com.gasperpintar.smokingtracker.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.Adapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.model.HistoryEntry
import com.gasperpintar.smokingtracker.model.HomeViewModel
import com.gasperpintar.smokingtracker.ui.DialogManager
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
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
    private val binding: FragmentHomeBinding get() = _binding!!

    private lateinit var database: Lazy<AppDatabase>
    private lateinit var historyAdapter: Adapter<HistoryEntry>
    private lateinit var homeViewModel: HomeViewModel

    private var selectedDate: LocalDate = LocalDate.now()
    private var lastEntry: HistoryEntity? = null

    private var timerJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        database = lazy { (requireActivity() as MainActivity).database }
        homeViewModel = HomeViewModel(database = database.value)

        setup()
        setupRecyclerView()
        refreshUI()

        return binding.root
    }

    private fun setup() {
        binding.buttonAddEntry.setOnClickListener {
            DialogManager.showInsertDialog(
                context = requireActivity(),
                layoutInflater = layoutInflater,
                database = database.value,
                lifecycleScope = lifecycleScope,
                refreshUI = {
                    homeViewModel.resetAchievementsCache()
                    updateLastEntry()
                    refreshUI()
                }
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
                    DialogManager.showEditDialog(
                        context = requireActivity(),
                        layoutInflater = layoutInflater,
                        database = database.value,
                        lifecycleScope = lifecycleScope,
                        entry = historyEntry,
                        refreshUI = {
                            homeViewModel.resetAchievementsCache()
                            updateLastEntry()
                            refreshUI()
                        }
                    )
                }

                deleteButton.setOnClickListener {
                    DialogManager.showDeleteDialog(
                        context = requireActivity(),
                        layoutInflater = layoutInflater,
                        database = database.value,
                        lifecycleScope = lifecycleScope,
                        entry = historyEntry,
                        refreshUI = {
                            homeViewModel.resetAchievementsCache()
                            updateLastEntry()
                            refreshUI()
                        }
                    )
                }
            },
            diffCallback = object : DiffUtil.ItemCallback<HistoryEntry>() {
                override fun areItemsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry) = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: HistoryEntry, newItem: HistoryEntry) = oldItem == newItem
            }
        )
        binding.recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewHistory.adapter = historyAdapter
    }

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
            lastEntry = database.value.historyDao().getLastHistoryEntry()
            updateTimerLabel()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (isActive) {
                updateTimerLabel()
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    @SuppressLint(value = ["DefaultLocale"])
    private fun updateTimerLabel() {
        val entry: HistoryEntity? = lastEntry

        val text: String = entry?.createdAt?.let { createdAt ->
            val duration: Duration = Duration.between(createdAt, LocalDateTime.now())
            val totalSeconds: Long = duration.seconds

            val days: Long = totalSeconds / 86400
            val hours: Long = (totalSeconds % 86400) / 3600
            val minutes: Long = (totalSeconds % 3600) / 60
            val seconds: Long = totalSeconds % 60

            val parts: MutableList<String> = mutableListOf()

            if (days > 0) {
                parts.add("$days${getString(R.string.home_timer_day)}")
            }

            if (hours > 0) {
                parts.add("$hours${getString(R.string.home_timer_hour)}")
            }

            if (minutes > 0) {
                parts.add("$minutes${getString(R.string.home_timer_minute)}")
            }

            parts.add("$seconds${getString(R.string.home_timer_second)}")
            parts.joinToString(" ")
        } ?: "0${getString(R.string.home_timer_second)}"

        binding.timerLabel.text = text

        if (lastEntry == null) {
            return
        }

        lifecycleScope.launch {
            homeViewModel.onLastEntryChanged(
                current = lastEntry?.let { HistoryEntry.fromEntity(entity = it) }
            )
        }
    }

    private suspend fun updateStatistics(date: LocalDate) {
        val (startOfDay, endOfDay) = TimeHelper.getDay(date)
        val dailyCount: Int = database.value.historyDao()
            .getHistoryCountBetween(start = startOfDay, end = endOfDay)

        val (startOfWeek, endOfWeek) = TimeHelper.getWeek(date)
        val weeklyCount: Int = database.value.historyDao()
            .getHistoryCountBetween(start = startOfWeek, end = endOfWeek)

        val (startOfMonth, endOfMonth) = TimeHelper.getMonth(date)
        val monthlyCount: Int = database.value.historyDao()
            .getHistoryCountBetween(start = startOfMonth, end = endOfMonth)

        binding.dailyValue.text = dailyCount.toString()
        binding.weeklyValue.text = weeklyCount.toString()
        binding.monthlyValue.text = monthlyCount.toString()
    }

    private suspend fun loadHistoryForDay(date: LocalDate) {
        val (startOfDay, endOfDay) = TimeHelper.getDay(date)

        val entityList: List<HistoryEntity> =
            database.value.historyDao().getHistoryBetween(start = startOfDay, end = endOfDay)

        val historyList: List<HistoryEntry> = entityList.map(transform = HistoryEntry::fromEntity)

        historyAdapter.submitList(historyList) {
            binding.recyclerviewHistory.scrollToPosition(0)
        }
    }

    override fun onResume() {
        super.onResume()
        startTimer()
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
        _binding = null
    }
}
