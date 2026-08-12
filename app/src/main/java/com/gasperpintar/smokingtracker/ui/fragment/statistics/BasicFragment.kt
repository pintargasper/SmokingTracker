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
import com.gasperpintar.smokingtracker.model.CigarettesPerDay
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

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
            binding.textMaxCigarettesDate.text = getLocalizedDate(day = maxResult?.day)

            val minResult: CigarettesPerDay? = historyRepository.getMinCigarettesPerDay()
            binding.textMinCigarettes.text = minResult?.dailySum?.toString() ?: "0"
            binding.textMinCigarettesDate.text = getLocalizedDate(day = minResult?.day)

            binding.textAverageCigarettes.text = String.format("%.2f", historyRepository.getAverageCigarettesPerDay())
            binding.textTotalCigarettes.text = historyRepository.getTotalCigarettes().toString()

            val firstRecordDate = withContext(Dispatchers.IO) {
                historyRepository.getFirstRecordDate()
            }

            val sinceFirstEntryString = if (firstRecordDate != null) {
                getDurationString(start = firstRecordDate)
            } else {
                resources.getQuantityString(R.plurals.time_hours, 0, 0)
            }
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
                } ?: formatMoney(value = 0.0)
            }
            binding.todaySpent.text = todaySpent

            val averagePerMonth = withContext(Dispatchers.IO) {
                allCosts.takeIf {
                    it.isNotEmpty()
                } ?.let {
                    getThisMonthSpent(allCosts = it)
                } ?: formatMoney(value = 0.0)
            }
            binding.monthSpent.text = averagePerMonth

            val mostExpensiveDay = withContext(Dispatchers.IO) {
                allCosts.takeIf {
                    it.isNotEmpty()
                } ?.let { costs ->
                    allHistory.takeIf {
                        it.isNotEmpty()
                    } ?.let { history ->
                        getMostExpensiveDay(allCosts = costs, history = history)
                    } ?: Pair(formatMoney(value = 0.0), null)
                } ?: Pair(formatMoney(value = 0.0), null)
            }
            binding.mostExpensiveDay.text = mostExpensiveDay.first
            binding.mostExpensiveDayDate.text = getLocalizedDate(day = mostExpensiveDay.second)
        }
    }

    private fun getLocalizedDate(
        day: String?
    ): String {
        return day?.let {
            val formattedDate: String = LocalizationHelper.formatDate(LocalDate.parse(it))
            getString(R.string.statistics_logged, formattedDate)
        } ?: ""
    }

    private fun getDurationString(
        start: LocalDateTime,
        end: LocalDateTime = LocalDateTime.now()
    ): String {
        val period = Period.between(start.toLocalDate(), end.toLocalDate())
        val duration = Duration.between(start.plusYears(period.years.toLong()).plusMonths(period.months.toLong()).plusDays(period.days.toLong()), end)

        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60

        return listOfNotNull(
            period.years.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_years, it, it)
            },
            period.months.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_months, it, it)
            },
            period.days.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_days, it, it)
            },
            hours.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_hours, it.toInt(), it)
            },
            minutes.takeIf { it > 0 }?.let {
                resources.getQuantityString(
                    R.plurals.time_minutes,
                    it.toInt(),
                    it
                )
            }
        ).joinToString(" ")
    }

    private fun getLongestTime(allHistory: List<HistoryEntity>): String {
        if (allHistory.isEmpty()) {
            return resources.getQuantityString(R.plurals.time_hours, 0, 0)
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
            getDurationString(start = longestInterval.first.createdAt, end = longestInterval.second.createdAt)
        } else {
            getDurationString(start = lastStart, end = now)
        }
    }

    private suspend fun getTotalSpent(allCosts: List<CostEntity>): String {
        val totalSpent: Double = historyRepository.getAll().sumOf {
            resolveCostAtTime(it.createdAt, allCosts)
        }
        return formatMoney(totalSpent)
    }

    private suspend fun getTodaySpent(allCosts: List<CostEntity>): String {
        val today: LocalDate = LocalDate.now()
        val dayStart: LocalDateTime = today.atStartOfDay()
        val dayEnd: LocalDateTime = today.plusDays(1).atStartOfDay()

        val totalSpent: Double = historyRepository.getBetween(start = dayStart, end = dayEnd).sumOf {
            resolveCostAtTime(it.createdAt, allCosts)
        }
        return formatMoney(totalSpent)
    }

    private suspend fun getThisMonthSpent(allCosts: List<CostEntity>): String {
        val today: LocalDate = LocalDate.now()
        val monthStart: LocalDateTime = today.withDayOfMonth(1).atStartOfDay()
        val monthEnd: LocalDateTime = today.withDayOfMonth(1).plusMonths(1).atStartOfDay()

        val totalSpent: Double = historyRepository.getBetween(start = monthStart, end = monthEnd).sumOf {
            resolveCostAtTime(it.createdAt, allCosts)
        }
        return formatMoney(totalSpent)
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
            formatMoney(value = maxSpent),
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

    private suspend fun formatMoney(value: Double): String {
        val currency: String = settingsRepository.get()?.currency ?: "€"
        return "${DecimalFormat("0.00#").format(value)} $currency"
    }
}