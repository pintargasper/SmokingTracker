package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.HomeState
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.LocalDate
import java.time.LocalDateTime

class HomeViewModel(
    private val achievementRepository: AchievementRepository,
    private val historyRepository: HistoryRepository
): ViewModel() {

    private var selectedDate = LocalDate.now()
    private var lastEntry: HistoryEntity? = null

    fun previousDay() {
        selectedDate = selectedDate.minusDays(1)
    }

    fun nextDay() {
        selectedDate = selectedDate.plusDays(1)
    }

    suspend fun getHistory(): HomeState {
        val date = selectedDate
        val (startOfDay, endOfDay) = TimeHelper.getDay(date)
        val historyEntities = historyRepository.getBetween(start = startOfDay, end = endOfDay)

        lastEntry = historyRepository.getLast()

        val history = historyEntities.map(transform = HistoryEntry::fromEntity)
        val dailyCount = historyRepository.getCountBetween(start = startOfDay, end = endOfDay)

        val (startOfWeek, endOfWeek) = TimeHelper.getWeek(date)
        val weeklyCount = historyRepository.getCountBetween(start = startOfWeek, end = endOfWeek)

        val (startOfMonth, endOfMonth) = TimeHelper.getMonth(date)
        val monthlyCount = historyRepository.getCountBetween(start = startOfMonth, end = endOfMonth)

        return HomeState(
            selectedDate = date,
            history = history,
            lastEntry = lastEntry,
            dailyCount = dailyCount,
            weeklyCount = weeklyCount,
            monthlyCount = monthlyCount
        )
    }

    suspend fun insert(
        isLent: Boolean
    ) {
        if (!isLent) achievementRepository.resetAll(state = true)
        historyRepository.insert(entry = HistoryEntity.default(isLent = isLent))
    }

    suspend fun update(
        entry: HistoryEntry,
        dateTime: LocalDateTime,
        isLent: Boolean
    ) {
        if (entry.id == lastEntry?.id) achievementRepository.resetAll(state = false)
        val updatedEntry = entry.copy(createdAt = dateTime, isLent = isLent)
        historyRepository.update(entry = updatedEntry.toEntity())
    }

    suspend fun delete(
        entry: HistoryEntry
    ) {
        if (entry.id == lastEntry?.id) achievementRepository.resetAll(state = false)
        historyRepository.delete(entry = entry.toEntity())
    }
}