package com.gasperpintar.smokingtracker.ui.fragment.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.StatisticsActivity
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.databinding.FragmentStatisticsBasicBinding
import com.gasperpintar.smokingtracker.database.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class BasicFragment : Fragment() {

    private var _binding: FragmentStatisticsBasicBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyRepository: HistoryRepository
    private lateinit var costsRepository: CostsRepository
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBasicBinding.inflate(inflater, container, false)

        val database = (requireActivity() as StatisticsActivity).database
        historyRepository = HistoryRepository(historyDao = database.historyDao())
        costsRepository = CostsRepository(costDao = database.costsDao())
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())

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
            binding.textMaxCigarettesDate.text = LocalizationHelper.formatLoggedDate(resources, day = maxResult?.day)

            val minResult: CigarettesPerDay? = historyRepository.getMinCigarettesPerDay()
            binding.textMinCigarettes.text = minResult?.dailySum?.toString() ?: "0"
            binding.textMinCigarettesDate.text = LocalizationHelper.formatLoggedDate(resources, day = minResult?.day)

            binding.textAverageCigarettes.text = String.format("%.2f", historyRepository.getAverageCigarettesPerDay())
            binding.textTotalCigarettes.text = String.format(Locale.getDefault(), "%d", historyRepository.getTotalCigarettes())

            val firstRecordDate = withContext(Dispatchers.IO) {
                historyRepository.getFirstRecordDate()
            }

            val sinceFirstEntryString = firstRecordDate?.let {
                TimeHelper.getDurationString(resources, start = it)
            } ?: resources.getQuantityString(R.plurals.time_minutes, 0, 0)
            binding.textSinceFirstEntry.text = sinceFirstEntryString

            val allHistory = withContext(Dispatchers.IO) {
                historyRepository.getAll()
            }
            binding.textLongestStreak.text = getLongestTime(allHistory)

            val allCosts = withContext(Dispatchers.IO) {
                costsRepository.getAll()
            }

            binding.statisticsTitleInstructions.isVisible = allCosts.isEmpty()

            val totalSpent = withContext(Dispatchers.IO) {
                getTotalSpent(allCosts)
            }
            binding.textTotalSpent.text = totalSpent

            val todaySpent = withContext(Dispatchers.IO) {
                allCosts.takeIf {
                    it.isNotEmpty()
                } ?.let {
                    getTodaySpent(allCosts = it)
                } ?: LocalizationHelper.formatMoney(settingsRepository, value = 0.0)
            }
            binding.todaySpent.text = todaySpent

            val averagePerMonth = withContext(Dispatchers.IO) {
                allCosts.takeIf {
                    it.isNotEmpty()
                } ?.let {
                    getThisMonthSpent(allCosts = it)
                } ?: LocalizationHelper.formatMoney(settingsRepository, value = 0.0)
            }
            binding.monthSpent.text = averagePerMonth

            val mostExpensiveDay = withContext(Dispatchers.IO) {
                allCosts.takeIf { it.isNotEmpty() } ?.let { costs ->
                    allHistory.takeIf { it.isNotEmpty() } ?.let { history ->
                        getMostExpensiveDay(allCosts = costs, history = history)
                    } ?: Pair(LocalizationHelper.formatMoney(settingsRepository, value = 0.0), null)
                } ?: Pair(LocalizationHelper.formatMoney(settingsRepository, value = 0.0), null)
            }
            binding.mostExpensiveDay.text = mostExpensiveDay.first
            binding.mostExpensiveDayDate.text = LocalizationHelper.formatLoggedDate(resources, day = mostExpensiveDay.second)
        }
    }

    private fun getLongestTime(allHistory: List<HistoryEntity>): String {
        if (allHistory.isEmpty()) {
            return resources.getQuantityString(R.plurals.time_minutes, 0, 0)
        }

        val now = LocalDateTime.now()
        val sortedHistory = allHistory.sortedBy { it.createdAt }

        val longestInterval = sortedHistory.zipWithNext().maxByOrNull { (previous, current) ->
            Duration.between(previous.createdAt, current.createdAt)
        }

        val lastStart = sortedHistory.last().createdAt
        val lastDuration = Duration.between(lastStart, now)

        return if (
            longestInterval != null &&
            Duration.between(longestInterval.first.createdAt, longestInterval.second.createdAt) > lastDuration) {
            TimeHelper.getDurationString(resources, start = longestInterval.first.createdAt, end = longestInterval.second.createdAt)
        } else {
            TimeHelper.getDurationString(resources, start = lastStart, end = now)
        }
    }

    private suspend fun getTotalSpent(allCosts: List<CostEntity>): String {
        val totalSpent: Double = historyRepository.getAll().sumOf {
            resolveCostAtTime(it.createdAt, allCosts)
        }
        return LocalizationHelper.formatMoney(settingsRepository, value = totalSpent)
    }

    private suspend fun getTodaySpent(allCosts: List<CostEntity>): String {
        val today: LocalDate = LocalDate.now()
        val dayStart: LocalDateTime = today.atStartOfDay()
        val dayEnd: LocalDateTime = today.plusDays(1).atStartOfDay()

        val totalSpent: Double = historyRepository.getBetween(start = dayStart, end = dayEnd).sumOf {
            resolveCostAtTime(it.createdAt, allCosts)
        }
        return LocalizationHelper.formatMoney(settingsRepository, value = totalSpent)
    }

    private suspend fun getThisMonthSpent(allCosts: List<CostEntity>): String {
        val today: LocalDate = LocalDate.now()
        val monthStart: LocalDateTime = today.withDayOfMonth(1).atStartOfDay()
        val monthEnd: LocalDateTime = today.withDayOfMonth(1).plusMonths(1).atStartOfDay()

        val totalSpent: Double = historyRepository.getBetween(start = monthStart, end = monthEnd).sumOf {
            resolveCostAtTime(it.createdAt, allCosts)
        }
        return LocalizationHelper.formatMoney(settingsRepository, value = totalSpent)
    }

    private suspend fun getMostExpensiveDay(allCosts: List<CostEntity>, history: List<HistoryEntity>): Pair<String, String> {
        val startDate: LocalDate = allCosts.minOf {
            it.startDate
        }.toLocalDate()

        val endDate: LocalDate = allCosts.maxOf {
            it.endDate
        }.toLocalDate()

        val cigarettesByDay: Map<LocalDate, Int> = history.groupBy {
            it.createdAt.toLocalDate()
        }.mapValues {
            it.value.size
        }

        var maxSpent = 0.0
        var currentDate: LocalDate = startDate
        var maxSpentDate: LocalDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val dayStart = currentDate.atStartOfDay()
            val cigarettesCount = cigarettesByDay[currentDate] ?: 0

            val activeCost: CostEntity? = allCosts.lastOrNull { cost ->
                !cost.startDate.isAfter(dayStart) && cost.endDate.isAfter(dayStart)
            }

            val daySpent: Double = activeCost?.let {
                cigarettesCount * it.price
            } ?: 0.0

            if (daySpent >= maxSpent) {
                maxSpent = daySpent
                maxSpentDate = currentDate
            }
            currentDate = currentDate.plusDays(1)
        }
        return Pair(
            LocalizationHelper.formatMoney(settingsRepository, value = maxSpent),
            maxSpentDate.toString()
        )
    }

    private fun resolveCostAtTime(
        time: LocalDateTime,
        allCosts: List<CostEntity>
    ): Double {
        return allCosts.firstOrNull { cost ->
            !time.isBefore(cost.startDate) && time.isBefore(cost.endDate)
        } ?.price ?: 0.0
    }
}