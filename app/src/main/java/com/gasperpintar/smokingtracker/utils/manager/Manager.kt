package com.gasperpintar.smokingtracker.utils.manager

import android.content.Context
import android.net.Uri
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.ui.bar.DataSyncPipeline
import com.gasperpintar.smokingtracker.ui.bar.SyncedStep
import com.gasperpintar.smokingtracker.utils.FileHelper
import com.gasperpintar.smokingtracker.utils.manager.Extensions.create
import com.gasperpintar.smokingtracker.utils.manager.Extensions.getRowCount
import com.gasperpintar.smokingtracker.utils.manager.Extensions.import
import com.gasperpintar.smokingtracker.utils.manager.Extensions.importSingleRow
import com.gasperpintar.smokingtracker.utils.manager.Mappers.toExcelRow
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.format.DateTimeFormatter

object Manager {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun downloadFile(
        context: Context,
        fileUri: Uri,
        achievementRepository: AchievementRepository,
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        notificationsSettingsRepository: NotificationsSettingsRepository,
        costsRepository: CostsRepository,
        notesRepository: NotesRepository,
        onProgress: (Int) -> Unit
    ): Uri = withContext(Dispatchers.IO) {
        val history = historyRepository.getAll()
        val achievements = achievementRepository.getAll()
        val costs = costsRepository.getAll()
        val notes = notesRepository.getAll()
        val settings = listOfNotNull(element = settingsRepository.get())
        val notifSettings = listOfNotNull(element = notificationsSettingsRepository.get())

        XSSFWorkbook().use { workbook ->
            DataSyncPipeline(
                steps = listOf(
                    SyncedStep(weight = history.size.coerceAtLeast(minimumValue = 1)) { progress ->
                        workbook.create(
                            name = "History",
                            headers = Mappers.HISTORY_HEADERS,
                            data = history,
                            onStepProgress = progress
                        ) {
                            it.toExcelRow(dateFormatter)
                        }
                    },
                    SyncedStep(weight = achievements.size.coerceAtLeast(minimumValue = 1)) { progress ->
                        workbook.create(
                            name = "Achievements",
                            headers = Mappers.ACHIEVEMENTS_HEADERS,
                            data = achievements,
                            onStepProgress = progress
                        ) {
                            it.toExcelRow(dateFormatter)
                        }
                    },
                    SyncedStep(weight = costs.size.coerceAtLeast(minimumValue = 1)) { progress ->
                        workbook.create(
                            name = "Costs",
                            headers = Mappers.COSTS_HEADERS,
                            data = costs,
                            onStepProgress = progress
                        ) {
                            it.toExcelRow(dateFormatter)
                        }
                    },
                    SyncedStep(weight = notes.size.coerceAtLeast(minimumValue = 1)) { progress ->
                        workbook.create(
                            name = "Notes",
                            headers = Mappers.NOTES_HEADERS,
                            data = notes,
                            onStepProgress = progress
                        ) {
                            it.toExcelRow(dateFormatter)
                        }
                    },
                    SyncedStep(weight = settings.size.coerceAtLeast(minimumValue = 1)) { progress ->
                        workbook.create(
                            name = "Settings",
                            headers = Mappers.SETTINGS_HEADERS,
                            data = settings,
                            onStepProgress = progress
                        ) {
                            it.toExcelRow()
                        }
                    },
                    SyncedStep(weight = notifSettings.size.coerceAtLeast(minimumValue = 1)) { progress ->
                        workbook.create(
                            name = "NotificationsSettings",
                            headers = Mappers.NOTIF_SETTINGS_HEADERS,
                            data = notifSettings,
                            onStepProgress = progress
                        ) {
                            it.toExcelRow()
                        }
                    },
                    SyncedStep(weight = 5) { progress ->
                        context.contentResolver.openOutputStream(fileUri)
                            ?.use(block = workbook::write)
                        progress(100)
                    }
                )
            ).run(onProgress)
        }

        sendNotification(
            context = context,
            title = context.getString(R.string.notification_download_title),
            content = context.getString(
                R.string.notification_download_content,
                FileHelper.getFileName(context, fileUri)
            ),
            notificationId = 1002,
            notificationsEnabled = notificationsSettingsRepository.get()?.system ?: true,
            fileUri = fileUri
        )
        fileUri
    }

    suspend fun uploadFile(
        context: Context,
        fileUri: Uri,
        achievementRepository: AchievementRepository,
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        notificationsSettingsRepository: NotificationsSettingsRepository,
        costsRepository: CostsRepository,
        notesRepository: NotesRepository,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val notificationsEnabled = notificationsSettingsRepository.get()?.system ?: true
        var success = false
        try {
            context.contentResolver.openInputStream(fileUri)?.use { stream ->
                XSSFWorkbook(stream).use { workbook ->
                    DataSyncPipeline(
                        steps = listOf(
                            SyncedStep(weight = workbook.getRowCount(sheetName = "History")) {
                                importHistorySheet(workbook, historyRepository, onStepProgress = it)
                            },
                            SyncedStep(weight = workbook.getRowCount(sheetName = "Achievements")) {
                                importAchievementSheet(
                                    workbook,
                                    achievementRepository,
                                    onStepProgress = it
                                )
                            },
                            SyncedStep(weight = workbook.getRowCount(sheetName = "Costs")) {
                                importCostsSheet(workbook, costsRepository, onStepProgress = it)
                            },
                            SyncedStep(weight = workbook.getRowCount(sheetName = "Notes")) {
                                importNotesSheet(workbook, notesRepository, onStepProgress = it)
                            },
                            SyncedStep(weight = workbook.getRowCount(sheetName = "Settings")) {
                                importSettingsSheet(
                                    workbook,
                                    settingsRepository,
                                    onStepProgress = it
                                )
                            },
                            SyncedStep(weight = workbook.getRowCount(sheetName = "NotificationsSettings")) {
                                importNotificationsSettingsSheet(
                                    workbook,
                                    notificationsSettingsRepository,
                                    onStepProgress = it
                                )
                            }
                        )
                    ).run(onProgress)
                }
            }
            success = true
        } finally {
            val title =
                context.getString(if (success) R.string.notification_upload_title else R.string.notification_upload_failed_title)
            val content =
                context.getString(if (success) R.string.notification_upload_content else R.string.notification_upload_failed_content)
            sendNotification(context, title, content, notificationId = 1002, notificationsEnabled)
        }
    }

    private suspend fun importHistorySheet(workbook: XSSFWorkbook, repository: HistoryRepository, onStepProgress: (Int) -> Unit) {
        val entities = workbook.import(sheetName = "History", requiredHeaders = Mappers.HISTORY_HEADERS, onStepProgress) { row, col, _ ->
            Mappers.parseHistory(row, col, dateFormatter)
        }
        repository.deleteAll()
        if (entities.isNotEmpty()) repository.insertAll(entries = entities)
    }

    private suspend fun importAchievementSheet(workbook: XSSFWorkbook, repository: AchievementRepository, onStepProgress: (Int) -> Unit) {
        val entities = workbook.import(sheetName = "Achievements", requiredHeaders = Mappers.ACHIEVEMENTS_HEADERS, onStepProgress) { row, col, index ->
            Mappers.parseAchievement(row, col, index, dateFormatter)
        }
        repository.deleteAll()
        if (entities.isNotEmpty()) repository.insert(entries = entities)
    }

    private suspend fun importCostsSheet(workbook: XSSFWorkbook, repository: CostsRepository, onStepProgress: (Int) -> Unit) {
        val entities = workbook.import(sheetName = "Costs", requiredHeaders = Mappers.COSTS_HEADERS, onStepProgress) { row, col, _ ->
            Mappers.parseCost(row, col, dateFormatter)
        }
        repository.deleteAll()
        if (entities.isNotEmpty()) repository.insertAll(entries = entities)
    }

    private suspend fun importNotesSheet(workbook: XSSFWorkbook, repository: NotesRepository, onStepProgress: (Int) -> Unit) {
        val entities = workbook.import(sheetName = "Notes", requiredHeaders = Mappers.NOTES_HEADERS, onStepProgress) { row, col, _ ->
            Mappers.parseNote(row, col, dateFormatter)
        }
        repository.deleteAll()
        if (entities.isNotEmpty()) repository.insertAll(entries = entities)
    }

    private suspend fun importSettingsSheet(workbook: XSSFWorkbook, repository: SettingsRepository, onStepProgress: (Int) -> Unit) {
        workbook.importSingleRow(sheetName = "Settings", onStepProgress) { row, col ->
            repository.get()?.let { repository.delete(settings = it) }
            repository.insert(settings = Mappers.parseSettings(row, col))
        }
    }

    private suspend fun importNotificationsSettingsSheet(workbook: XSSFWorkbook, repository: NotificationsSettingsRepository, onStepProgress: (Int) -> Unit) {
        workbook.importSingleRow(sheetName = "NotificationsSettings", onStepProgress) { row, col ->
            repository.get()?.let { repository.delete(settings = it) }
            repository.insert(settings = Mappers.parseNotificationsSettings(row, col))
        }
    }

    private fun sendNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int,
        notificationsEnabled: Boolean,
        fileUri: Uri? = null
    ) {
        if (!notificationsEnabled) return

        val mainActivity = context as? MainActivity ?: return
        if (!mainActivity.permissionsHelper.isNotificationPermissionGranted()) return

        val safeUri = fileUri?.takeIf { it.scheme == "content" }
        Notifications.sendNotification(context, title, content, notificationId, safeUri)
    }
}