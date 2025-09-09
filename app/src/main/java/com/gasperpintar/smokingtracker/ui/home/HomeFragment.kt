package com.gasperpintar.smokingtracker.ui.home

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.adapter.history.HistoryAdapter
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.databinding.FragmentHomeBinding
import com.gasperpintar.smokingtracker.model.HistoryEntry
import com.gasperpintar.smokingtracker.utils.Helper
import com.gasperpintar.smokingtracker.utils.Helper.toHistoryEntity
import com.gasperpintar.smokingtracker.utils.Helper.toHistoryEntry
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
            insertDialog()
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
                editDialog(entry)
            },

            onDeleteClick = { entry ->
                deleteDialog(entry)
            }
        )
        binding.recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewHistory.adapter = historyAdapter
    }

    private fun refreshUI() {
        binding.currentDate.text = getString(
            R.string.home_current_date_format,
            selectedDate.dayOfMonth,
            selectedDate.monthValue,
            selectedDate.year
        )

        lifecycleScope.launch {
            updateStatistics(selectedDate)
            loadHistoryForDay(selectedDate)
        }
        updateLastEntry()
    }

    private fun insertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.insert_popup, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_insert)
        val buttonClose = dialogView.findViewById<Button>(R.id.button_close)
        val lentCheckbox = dialogView.findViewById<CheckBox>(R.id.lent_checkbox)

        buttonConfirm.setOnClickListener {
            val entry = HistoryEntity (
                id = 0,
                lent = if (lentCheckbox.isChecked) 1  else 0,
                createdAt = LocalDateTime.now()
            )

            lifecycleScope.launch {
                database.value.historyDao().insert(history = entry)
                refreshUI()
            }
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun editDialog(entry: HistoryEntry) {
        val dialogView = layoutInflater.inflate(R.layout.edit_popup, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_confirm)
        val buttonClose = dialogView.findViewById<Button>(R.id.button_close)
        val lentCheckbox = dialogView.findViewById<CheckBox>(R.id.lent_checkbox)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.date_picker)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.time_picker)

        lentCheckbox.isChecked = entry.isLent
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))

        entry.createdAt.let { dateTime ->
            datePicker.updateDate(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth)
            timePicker.hour = dateTime.hour
            timePicker.minute = dateTime.minute
        }

        buttonConfirm.setOnClickListener {
            val entry: HistoryEntry = entry.copy(
                createdAt = entry.createdAt.withYear(datePicker.year)
                    .withMonth(datePicker.month + 1)
                    .withDayOfMonth(datePicker.dayOfMonth)
                    .withHour(timePicker.hour)
                    .withMinute(timePicker.minute),
                isLent = lentCheckbox.isChecked
            )

            lifecycleScope.launch {
                database.value.historyDao().update(history = entry.toHistoryEntity())
                refreshUI()
            }
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteDialog(entry: HistoryEntry) {
        val dialogView = layoutInflater.inflate(R.layout.delete_popup, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_confirm)
        val buttonClose = dialogView.findViewById<Button>(R.id.button_close)

        buttonConfirm.setOnClickListener {
            lifecycleScope.launch {
                database.value.historyDao().delete(history = entry.toHistoryEntity())
                refreshUI()
            }
            dialog.dismiss()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateLastEntry() {
        lifecycleScope.launch {
            lastEntry = database.value.historyDao()
                .getLastHistoryEntry(endOfToday = Helper.getEndOfDay())
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

    private fun updateTimerLabel() {
        val entry = lastEntry

        val timeDifference: String = if (entry?.createdAt != null) {
            val duration = Duration.between(entry.createdAt, LocalDateTime.now())
            getString(R.string.home_timer_label,
                duration.toHours(),
                duration.toMinutes() % 60,
                duration.seconds % 60
            )
        } else {
            "--:--:--"
        }
        binding.timerLabel.text = timeDifference
    }

    private suspend fun updateStatistics(date: LocalDate) {
        val (startOfDay, endOfDay) = Helper.getDay(date)
        val dailyCount = database.value.historyDao().getHistoryCountBetween(startOfDay, endOfDay)

        val (startOfWeek, endOfWeek) = Helper.getWeek(date)
        val weeklyCount = database.value.historyDao().getHistoryCountBetween(startOfWeek, endOfWeek)

        val (startOfMonth, endOfMonth) = Helper.getMonth(date)
        val monthlyCount = database.value.historyDao().getHistoryCountBetween(startOfMonth, endOfMonth)

        binding.dailyValue.text = dailyCount.toString()
        binding.weeklyValue.text = weeklyCount.toString()
        binding.monthlyValue.text = monthlyCount.toString()
    }

    private suspend fun loadHistoryForDay(date: LocalDate) {
        val (startOfDay, endOfDay) = Helper.getDay(date)
        val entityList = database.value.historyDao().getHistoryBetween(startOfDay, endOfDay)
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