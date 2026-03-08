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
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBasicBinding
import com.gasperpintar.smokingtracker.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

class BasicFragment : Fragment() {

    private var _binding: FragmentStatisticsBasicBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var historyRepository: HistoryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBasicBinding.inflate(inflater, container, false)

        database = (requireActivity() as StatisticsActivity).database
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
            binding.textMaxCigarettesDate.text = getLocalizedDate(maxResult?.day)

            val minResult: CigarettesPerDay? = historyRepository.getMinCigarettesPerDay()
            binding.textMinCigarettes.text = minResult?.dailySum?.toString() ?: "0"
            binding.textMinCigarettesDate.text = getLocalizedDate(minResult?.day)

            binding.textAverageCigarettes.text = String.format("%.2f", historyRepository.getAverageCigarettesPerDay())
            binding.textTotalCigarettes.text = historyRepository.getTotalCigarettes().toString()

            val firstRecordDate = withContext(Dispatchers.IO) {
                historyRepository.getFirstRecordDate()
            }

            val sinceFirstEntryString = if (firstRecordDate != null) {
                getDurationString(firstRecordDate)
            } else {
                resources.getQuantityString(R.plurals.time_days, 0, 0)
            }
            binding.textSinceFirstEntry.text = sinceFirstEntryString

            val allHistory = withContext(Dispatchers.IO) {
                historyRepository.getAll()
            }
            binding.textLongestStreak.text = getLongestTime(allHistory).toString()
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
        val parts = listOfNotNull(period.years.takeIf {
            it > 0
        } ?.let {
            resources.getQuantityString(R.plurals.time_years, it, it)
                },
            period.months.takeIf {
                it > 0
            } ?.let {
                resources.getQuantityString(R.plurals.time_months, it, it)
                    }, (period.days.takeIf { it > 0 || (period.years == 0 && period.months == 0) }) ?.let {
                resources.getQuantityString(
                    R.plurals.time_days,
                    it,
                    it
                )
            }
        )
        return parts.joinToString(" ")
    }

    private fun getLongestTime(allHistory: List<HistoryEntity>): Int {
        if (allHistory.isEmpty()) {
            return 0
        }

        val firstDate = allHistory.minByOrNull {
            it.createdAt
        }?.createdAt?.toLocalDate() ?: return 0

        val smokedDays = allHistory
            .filter { it.lent == 0 }
            .map { it.createdAt.toLocalDate() }
            .toSet()

        var maxStreak = 0
        var currentStreak = 0
        var date = firstDate

        while (!date.isAfter(LocalDateTime.now().toLocalDate().minusDays(1))) {
            if (!smokedDays.contains(date)) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
            date = date.plusDays(1)
        }
        return maxStreak
    }
}