package com.gasperpintar.smokingtracker.database.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsUiState
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.Manager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val achievementRepository: AchievementRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationsSettingsRepository: NotificationsSettingsRepository,
    private val costsRepository: CostsRepository,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val settings = settingsRepository.get()
            val notificationsSettings = notificationsSettingsRepository.get()
            val costs = costsRepository.getAll().map(transform = CostEntry::fromEntity)

            _uiState.value = SettingsUiState(
                settings = settings,
                notificationsSettings = notificationsSettings,
                costs = costs
            )
        }
    }

    suspend fun updateSettings(
        updateBlock: (SettingsEntity) -> SettingsEntity
    ) {
            val currentSettings = settingsRepository.get() ?: return
            val updatedSettings = updateBlock(currentSettings)

            settingsRepository.update(settings = updatedSettings)
            _uiState.value = _uiState.value.copy(settings = updatedSettings)
    }

    fun updateNotificationSettings(
        settings: NotificationsSettingsEntity
    ) {
        viewModelScope.launch {
            notificationsSettingsRepository.update(settings = settings)
            _uiState.value = _uiState.value.copy(notificationsSettings = settings)
        }
    }

    suspend fun addCost(cost: CostEntry) {
        costsRepository.insert(entry = cost.toEntity())
        val updatedCosts = costsRepository.getAll().map(transform = CostEntry::fromEntity)
        _uiState.update { it.copy(costs = updatedCosts) }
    }

    suspend fun deleteCost(cost: CostEntry) {
        costsRepository.delete(entry = cost.toEntity())
        val updatedCosts = costsRepository.getAll().map(transform = CostEntry::fromEntity)
        _uiState.update { it.copy(costs = updatedCosts) }
    }

    fun restoreFile(
        context: Context,
        fileUri: Uri,
        onProgress: (Int) -> Unit,
        onFinished: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                Manager.uploadFile(
                    context = context,
                    fileUri = fileUri,
                    achievementRepository = achievementRepository,
                    historyRepository = historyRepository,
                    settingsRepository = settingsRepository,
                    notificationsSettingsRepository = notificationsSettingsRepository,
                    costsRepository = costsRepository,
                    notesRepository = notesRepository,
                    onProgress = onProgress
                )
                onFinished()
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun exportFile(
        context: Context,
        fileUri: Uri,
        onProgress: (Int) -> Unit,
        onFinished: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                Manager.downloadFile(
                    context = context,
                    fileUri = fileUri,
                    achievementRepository = achievementRepository,
                    historyRepository = historyRepository,
                    settingsRepository = settingsRepository,
                    notificationsSettingsRepository = notificationsSettingsRepository,
                    costsRepository = costsRepository,
                    notesRepository = notesRepository,
                    onProgress = onProgress
                )
                onFinished()
            } catch (_: Exception) {
                onError()
            }
        }
    }
}