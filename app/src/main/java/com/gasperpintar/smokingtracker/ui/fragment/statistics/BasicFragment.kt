package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.StatisticsActivity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBasicBinding
import com.gasperpintar.smokingtracker.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

class BasicFragment : Fragment() {

    private var _binding: FragmentStatisticsBasicBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyRepository: HistoryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBasicBinding.inflate(inflater, container, false)

        val database = (requireActivity() as StatisticsActivity).database
        historyRepository = HistoryRepository(historyDao = database.historyDao())

        setup()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint(value = ["DefaultLocale"])
    private fun setup() {
        lifecycleScope.launch {
            val maxResult: CigarettesPerDay? = historyRepository.getMaxCigarettesPerDay()
            binding.textMaxCigarettes.text = maxResult?.dailySum?.toString() ?: "0"
            binding.textMaxCigarettesDate.text = maxResult?.day?.let { date ->
                getString(R.string.statistics_logged, getLocalizedDate(day = date))
            } ?: ""

            val minResult: CigarettesPerDay? = historyRepository.getMinCigarettesPerDay()
            binding.textMinCigarettes.text = minResult?.dailySum?.toString() ?: "0"
            binding.textMinCigarettesDate.text = minResult?.day?.let { date ->
                getString(R.string.statistics_logged, getLocalizedDate(day = date))
            } ?: ""

            binding.textAverageCigarettes.text = String.format("%.2f", historyRepository.getAverageCigarettesPerDay())
            binding.textTotalCigarettes.text = historyRepository.getTotalCigarettes().toString()

            val firstRecordDate = withContext(Dispatchers.IO) {
                historyRepository.getFirstRecordDate()
            }

            val sinceFirstEntryString = if (firstRecordDate != null) {
                getDurationString(start = firstRecordDate)
            } else {
                resources.getQuantityString(R.plurals.time_days, 0, 0)
            }
            binding.textSinceFirstEntry.text = sinceFirstEntryString

            val allHistory = withContext(Dispatchers.IO) {
                historyRepository.getAll()
            }
            binding.textLongestStreak.text = getLongestTime(allHistory)
        }
    }

    private fun getLocalizedDate(
        day: String?
    ): String {
        return day?.let {
            LocalizationHelper.formatDate(LocalDate.parse(it))
        } ?: "-"
    }

    private fun getDurationString(
        start: LocalDateTime
    ): String {
        val period = Period.between(start.toLocalDate(), LocalDateTime.now().toLocalDate())
        val startWithPeriodAdded = start.plusYears(period.years.toLong())
            .plusMonths(period.months.toLong())
            .plusDays(period.days.toLong())
        val duration = Duration.between(startWithPeriodAdded, LocalDateTime.now())
        val hours = duration.toHours()

        val parts = listOfNotNull(
            period.years.takeIf {
                it > 0
            }?.let {
                resources.getQuantityString(R.plurals.time_years, it, it)
            },
            period.months.takeIf {
                it > 0
            }?.let {
                resources.getQuantityString(R.plurals.time_months, it, it)
            },
            (period.days.takeIf {
                it > 0 || (period.years == 0 && period.months == 0)
            })?.let {
                resources.getQuantityString(R.plurals.time_days, it, it)
            },
            hours.takeIf {
                it > 0
            }?.let {
                resources.getQuantityString(R.plurals.time_hours, it.toInt(), it)
            }
        )
        return parts.joinToString(" ")
    }

    private fun getLongestTime(
        allHistory: List<HistoryEntity>
    ): String {
        if (allHistory.isEmpty()) {
            return resources.getQuantityString(R.plurals.time_hours, 0, 0)
        }

        val sortedHistory = allHistory.sortedBy { it.createdAt }
        var longestStart: LocalDateTime = sortedHistory[0].createdAt
        var longestEnd: LocalDateTime = sortedHistory[0].createdAt
        var longestDuration: Duration = Duration.ZERO

        for (i in 1 until sortedHistory.size) {
            val previousEntry = sortedHistory[i - 1]
            val currentEntry = sortedHistory[i]

            val gapDuration = Duration.between(previousEntry.createdAt, currentEntry.createdAt)
            if (gapDuration > longestDuration) {
                longestDuration = gapDuration
                longestStart = previousEntry.createdAt
                longestEnd = currentEntry.createdAt
            }
        }

        val gapToNow = Duration.between(sortedHistory.last().createdAt, LocalDateTime.now())
        if (gapToNow > longestDuration) {
            longestStart = sortedHistory.last().createdAt
            longestEnd = LocalDateTime.now()
        }

        val duration = Duration.between(longestStart, longestEnd)
        val totalMinutes = duration.toMinutes()
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60

        val parts = mutableListOf<String>()
        if (days > 0) {
            parts.add(resources.getQuantityString(R.plurals.time_days, days.toInt(), days))
        }

        if (hours > 0 || days == 0L) {
            parts.add(resources.getQuantityString(R.plurals.time_hours, hours.toInt(), hours))
        }

        if (minutes > 0 || (hours == 0L && days == 0L)) {
            parts.add(resources.getQuantityString(R.plurals.time_minutes, minutes.toInt(), minutes))
        }
        return parts.joinToString(" ")
    }
}