package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.HomeUiState
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class HomeViewModel(
    private val achievementRepository: AchievementRepository,
    private val historyRepository: HistoryRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(value = HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val (startOfDay, endOfDay) = TimeHelper.getDay(date)

            val historyEntities = historyRepository.getBetween(start = startOfDay, end = endOfDay)
            val history = historyEntities.map(transform = HistoryEntry::fromEntity)
            val dailyCount = historyRepository.getCountBetween(start = startOfDay, end = endOfDay)

            val (startOfWeek, endOfWeek) = TimeHelper.getWeek(date)
            val weeklyCount = historyRepository.getCountBetween(start = startOfWeek, end = endOfWeek)

            val (startOfMonth, endOfMonth) = TimeHelper.getMonth(date)
            val monthlyCount = historyRepository.getCountBetween(start = startOfMonth, end = endOfMonth)

            val lastEntry = historyRepository.getLast()
            _uiState.value = _uiState.value.copy(
                history = history,
                dailyCount = dailyCount,
                weeklyCount = weeklyCount,
                monthlyCount = monthlyCount,
                lastEntry = lastEntry
            )
        }
    }

    fun selectDate(
        date: LocalDate
    ) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        load()
    }

    fun insert(
        isLent: Boolean
    ) {
        viewModelScope.launch {
            achievementRepository.resetAll(state = true)
            historyRepository.insert(entry = HistoryEntity.default(isLent = isLent))
            load()
        }
    }

    fun update(
        entry: HistoryEntry,
        dateTime: LocalDateTime,
        isLent: Boolean
    ) {
        viewModelScope.launch {
            if (_uiState.value.lastEntry?.id == entry.id) {
                achievementRepository.resetAll(state = false)
            }

            val updatedEntry = entry.copy(createdAt = dateTime, isLent = isLent)
            historyRepository.update(entry = updatedEntry.toEntity())
            load()
        }
    }

    fun delete(
        entry: HistoryEntry
    ) {
        viewModelScope.launch {
            if (_uiState.value.lastEntry?.id == entry.id) {
                achievementRepository.resetAll(state = false)
            }

            historyRepository.delete(entry = entry.toEntity())
            load()
        }
    }
}