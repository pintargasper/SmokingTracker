package com.gasperpintar.smokingtracker.database.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsState
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.Manager

class SettingsViewModel(
    private val achievementRepository: AchievementRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationsSettingsRepository: NotificationsSettingsRepository,
    private val costsRepository: CostsRepository,
    private val notesRepository: NotesRepository
): ViewModel() {

    suspend fun getSettings(): SettingsState {
        val settings = settingsRepository.get()
        val notificationsSettings = notificationsSettingsRepository.get()
        val costs = costsRepository.getAll().map(transform = CostEntry::fromEntity)

        return SettingsState(
            settings = settings!!,
            notificationsSettings = notificationsSettings!!,
            costs = costs
        )
    }

    suspend fun updateSettings(
        updateBlock: (SettingsEntity) -> SettingsEntity
    ) {
        val currentSettings = settingsRepository.get() ?: return
        val updatedSettings = updateBlock(currentSettings)
        settingsRepository.update(settings = updatedSettings)
    }

    suspend fun updateNotificationSettings(
        settings: NotificationsSettingsEntity
    ) {
        notificationsSettingsRepository.update(settings = settings)
    }

    suspend fun addCost(cost: CostEntry) {
        costsRepository.insert(entry = cost.toEntity())
    }

    suspend fun deleteCost(cost: CostEntry) {
        costsRepository.delete(entry = cost.toEntity())
    }

    suspend fun restoreFile(
        context: Context,
        fileUri: Uri,
        onProgress: (Int) -> Unit,
        onFinished: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
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

    suspend fun exportFile(
        context: Context,
        fileUri: Uri,
        onProgress: (Int) -> Unit,
        onFinished: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
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