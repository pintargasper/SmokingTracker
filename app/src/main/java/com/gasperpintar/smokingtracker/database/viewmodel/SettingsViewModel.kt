package com.gasperpintar.smokingtracker.database.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.model.NotificationsEntry
import com.gasperpintar.smokingtracker.database.model.SettingsEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.SettingsState
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotesRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementEvaluator
import com.gasperpintar.smokingtracker.utils.manager.Manager
import java.time.LocalDateTime

class SettingsViewModel(
    private val achievementRepository: AchievementRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val costsRepository: CostsRepository,
    private val notesRepository: NotesRepository
): ViewModel() {

    suspend fun getState(): SettingsState {
        val settings = settingsRepository.get()
        val notificationsSettings = notificationsRepository.get()
        val costs = costsRepository.getAll()

        return SettingsState(
            settings = settings!!,
            notificationsSettings = notificationsSettings!!,
            costs = costs
        )
    }

    suspend fun updateSettings(
        settings: SettingsEntry
    ) {
        settingsRepository.update(settings = settings)
    }

    suspend fun updateNotificationSettings(
        settings: NotificationsEntry
    ) {
        notificationsRepository.update(settings = settings)
    }

    suspend fun addCost(
        cost: CostEntry
    ) {
        costsRepository.insert(entry = cost)
    }

    suspend fun deleteCost(
        cost: CostEntry
    ) {
        costsRepository.delete(entry = cost)
    }

    suspend fun restoreFile(
        context: Context,
        fileUri: Uri,
        onProgress: (Int) -> Unit,
        onFinished: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        try {
            Manager.restore(
                context = context,
                fileUri = fileUri,
                achievementRepository = achievementRepository,
                historyRepository = historyRepository,
                settingsRepository = settingsRepository,
                notificationsRepository = notificationsRepository,
                costsRepository = costsRepository,
                notesRepository = notesRepository,
                onProgress = onProgress
            )

            (context.applicationContext as? Application)?.applyTheme(
                themeId = settingsRepository.get()?.theme ?: 0
            )

            val lastSmoke = historyRepository.getLast()
            if (lastSmoke != null) {
                AchievementEvaluator(
                    context = context,
                    historyRepository = historyRepository,
                    achievementRepository = achievementRepository,
                    notificationsRepository = notificationsRepository
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
            Manager.export(
                context = context,
                fileUri = fileUri,
                achievementRepository = achievementRepository,
                historyRepository = historyRepository,
                settingsRepository = settingsRepository,
                notificationsRepository = notificationsRepository,
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