package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.HomeState
import com.gasperpintar.smokingtracker.database.viewmodel.state.MinimalHomeState
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.time.LocalDate
import java.time.LocalDateTime

class HomeViewModel(
    private val achievementRepository: AchievementRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private var selectedDate: LocalDate? = null
    private var endMinutes: Int? = null
    private var lastEntry: HistoryEntity? = null
    private val insertMutex = Mutex()
    private val _isAdding = MutableStateFlow(false)
    private val _addEvents = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    val historyChanges = historyRepository.changes
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()
    val addEvents: SharedFlow<Boolean> = _addEvents.asSharedFlow()

    fun previousDay() {
        selectedDate = selectedDate!!.minusDays(1)
    }

    fun nextDay() {
        selectedDate = selectedDate!!.plusDays(1)
    }

    suspend fun getState(): HomeState {
        val dayEndMinutes = settingsRepository.get()!!.dayEndMinutes
        if (selectedDate == null) selectedDate = TimeHelper.dayDate(dayEndMinutes)
        if (endMinutes != dayEndMinutes) {
            selectedDate = TimeHelper.updateDayDate(
                selectedDate = selectedDate!!,
                oldDayEndMinutes = endMinutes ?: 0,
                newDayEndMinutes = dayEndMinutes
            )
            endMinutes = dayEndMinutes
        }
        lastEntry = historyRepository.getLast()

        val (startOfDay, endOfDay) = TimeHelper.getDay(date = selectedDate!!, dayEndMinutes = dayEndMinutes)
        val historyEntities = historyRepository.getBetween(start = startOfDay, end = endOfDay)

        val history = historyEntities.map(transform = HistoryEntry::fromEntity)
        val dailyCount = historyRepository.getCountBetween(start = startOfDay, end = endOfDay)

        val (startOfWeek, endOfWeek) = TimeHelper.getWeek(date = selectedDate!!, dayEndMinutes = dayEndMinutes)
        val weeklyCount = historyRepository.getCountBetween(start = startOfWeek, end = endOfWeek)

        val (startOfMonth, endOfMonth) = TimeHelper.getMonth(date = selectedDate!!, dayEndMinutes = dayEndMinutes)
        val monthlyCount = historyRepository.getCountBetween(start = startOfMonth, end = endOfMonth)

        return HomeState(
            selectedDate = selectedDate!!,
            history = history,
            lastEntry = lastEntry,
            dailyCount = dailyCount,
            weeklyCount = weeklyCount,
            monthlyCount = monthlyCount
        )
    }

    suspend fun getMinimalState(now: LocalDateTime = LocalDateTime.now()): MinimalHomeState {
        val settings = settingsRepository.get()!!
        val dayEndMinutes = settings.dayEndMinutes
        val today = TimeHelper.dayDate(dateTime = now, dayEndMinutes = dayEndMinutes)
        val (startOfDay, endOfDay) = TimeHelper.getDay(date = today, dayEndMinutes = dayEndMinutes)
        return MinimalHomeState(
            lastEntry = historyRepository.getLast(),
            smokedToday = historyRepository.getSmokedCountBetween(start = startOfDay, end = endOfDay),
            dayEndMinutes = dayEndMinutes,
            elapsedTimeWarningMinutes = settings.elapsedTimeWarningMinutes
        )
    }

    fun insertNow() {
        if (!insertMutex.tryLock()) return
        viewModelScope.launch {
            _isAdding.value = true
            try {
                insert(isLent = false)
                _addEvents.emit(true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _addEvents.emit(false)
            } finally {
                _isAdding.value = false
                insertMutex.unlock()
            }
        }
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
