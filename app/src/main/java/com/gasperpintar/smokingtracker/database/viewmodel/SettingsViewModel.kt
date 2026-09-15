package com.gasperpintar.smokingtracker.database.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsState
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotesRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementEvaluator
import com.gasperpintar.smokingtracker.utils.manager.Manager
import java.time.LocalDateTime

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
        settings: SettingsEntity
    ) {
        settingsRepository.update(settings = settings)
    }

    suspend fun updateNotificationSettings(
        settings: NotificationsSettingsEntity
    ) {
        notificationsSettingsRepository.update(settings = settings)
    }

    suspend fun addCost(
        cost: CostEntry
    ) {
        costsRepository.insert(entry = cost.toEntity())
    }

    suspend fun deleteCost(
        cost: CostEntry
    ) {
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

            val lastSmoke = historyRepository.getLast()
            if (lastSmoke != null) {
                AchievementEvaluator(
                    context = context,
                    historyRepository = historyRepository,
                    achievementRepository = achievementRepository,
                    notificationsSettingsRepository = notificationsSettingsRepository
                ).evaluate(lastSmokeTime = lastSmoke.createdAt, now = LocalDateTime.now())
            }
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