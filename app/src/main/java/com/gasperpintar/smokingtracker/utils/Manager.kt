package com.gasperpintar.smokingtracker.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Manager {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun downloadFile(
        context: Context,
        achievementRepository: AchievementRepository,
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        notificationsSettingsRepository: NotificationsSettingsRepository,
        fileName: String = "st_data.xlsx"
    ): Uri? = withContext(context = Dispatchers.IO) {
        XSSFWorkbook().use { workbook ->
            createAchievementSheet(workbook, achievementList = achievementRepository.getAll())
            createHistorySheet(workbook, historyList = historyRepository.getAll())
            createSettingsSheet(workbook, settings = settingsRepository.get())
            createNotificationsSettingsSheet(workbook, notificationsSettings = notificationsSettingsRepository.get())
            val fileUri = saveWorkbookToFile(context, workbook, fileName)
            sendNotification(
                context,
                title = context.getString(R.string.notification_download_title),
                content = context.getString(
                    R.string.notification_download_content,
                    fileName
                ),
                notificationId = 1002,
                fileUri = fileUri,
                notificationsEnabled = notificationsSettingsRepository.get()!!.system
            )
            return@withContext fileUri
        }
    }

    suspend fun uploadFile(context: Context, 
                           fileUri: Uri,
                           achievementRepository: AchievementRepository,
                           historyRepository: HistoryRepository,
                           settingsRepository: SettingsRepository,
                           notificationsSettingsRepository: NotificationsSettingsRepository
    ) = withContext(
        context = Dispatchers.IO
    ) {
        val notificationsEnabled = notificationsSettingsRepository.get()!!.system
        try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                XSSFWorkbook(inputStream).use { workbook ->
                    importHistorySheet(workbook, historyRepository = historyRepository)
                    importSettingsSheet(workbook, settingsRepository = settingsRepository)
                    importAchievementSheet(workbook, achievementRepository = achievementRepository)
                    importNotificationsSettingsSheet(workbook, notificationsSettingsRepository = notificationsSettingsRepository)
                }
            }
            sendNotification(
                context,
                title = context.getString(R.string.notification_upload_title),
                content = context.getString(R.string.notification_upload_content),
                notificationId = 1002,
                notificationsEnabled = notificationsEnabled
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            sendNotification(
                context,
                title = context.getString(R.string.notification_upload_failed_title),
                content = context.getString(R.string.notification_upload_failed_content),
                notificationId = 1002,
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    private fun createHistorySheet(
        workbook: XSSFWorkbook,
        historyList: List<HistoryEntity>
    ) {
        val sheet = workbook.createSheet("History")
        val header = sheet.createRow(0)
        header.createCell(0, CellType.STRING).setCellValue("Lent")
        header.createCell(1, CellType.STRING).setCellValue("CreatedAt")

        historyList.forEachIndexed { index, history ->
            val row = sheet.createRow(index + 1)
            row.createCell(0, CellType.NUMERIC).setCellValue(history.lent.toDouble())
            row.createCell(1, CellType.STRING).setCellValue(history.createdAt.format(dateFormatter))
        }
    }

    private fun createSettingsSheet(
        workbook: XSSFWorkbook,
        settings: SettingsEntity?
    ) {
        val sheet = workbook.createSheet("Settings")
        val header = sheet.createRow(0)
        header.createCell(0, CellType.STRING).setCellValue("Theme")
        header.createCell(1, CellType.STRING).setCellValue("Language")

        settings?.let {
            val row = sheet.createRow(1)
            row.createCell(0, CellType.NUMERIC).setCellValue(it.theme.toDouble())
            row.createCell(1, CellType.NUMERIC).setCellValue(it.language.toDouble())
        }
    }

    private fun createAchievementSheet(
        workbook: XSSFWorkbook,
        achievementList: List<AchievementEntity>
    ) {
        val sheet = workbook.createSheet("Achievements")
        val header = sheet.createRow(0)
        header.createCell(0, CellType.NUMERIC).setCellValue("Image")
        header.createCell(1, CellType.NUMERIC).setCellValue("Value")
        header.createCell(2, CellType.STRING).setCellValue("Message")
        header.createCell(3, CellType.NUMERIC).setCellValue("Times")
        header.createCell(4, CellType.STRING).setCellValue("LastAchieved")
        header.createCell(5, CellType.BOOLEAN).setCellValue("Reset")
        header.createCell(6, CellType.BOOLEAN).setCellValue("Notify")
        header.createCell(7, CellType.STRING).setCellValue("Category")
        header.createCell(8, CellType.STRING).setCellValue("Unit")
        achievementList.forEachIndexed { index, achievement ->
            val row = sheet.createRow(index + 1)
            row.createCell(0, CellType.NUMERIC).setCellValue(achievement.image.toDouble())
            row.createCell(1, CellType.NUMERIC).setCellValue(achievement.value.toDouble())
            row.createCell(2, CellType.STRING).setCellValue(achievement.message)
            row.createCell(3, CellType.NUMERIC).setCellValue(achievement.times.toDouble())
            row.createCell(4, CellType.STRING).setCellValue(achievement.lastAchieved?.format(dateFormatter) ?: "")
            row.createCell(5, CellType.BOOLEAN).setCellValue(achievement.reset)
            row.createCell(6, CellType.BOOLEAN).setCellValue(achievement.notify)
            row.createCell(7, CellType.STRING).setCellValue(achievement.category.name)
            row.createCell(8, CellType.STRING).setCellValue(achievement.unit.name)
        }
    }

    private fun createNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        notificationsSettings: NotificationsSettingsEntity?
    ) {
        val sheet = workbook.createSheet("NotificationsSettings")
        val header = sheet.createRow(0)
        header.createCell(0, CellType.BOOLEAN).setCellValue("System")
        header.createCell(1, CellType.BOOLEAN).setCellValue("Achievements")
        notificationsSettings?.let {
            val row = sheet.createRow(1)
            row.createCell(0, CellType.BOOLEAN).setCellValue(it.system)
            row.createCell(1, CellType.BOOLEAN).setCellValue(it.achievements)
        }
    }

    private suspend fun importHistorySheet(
        workbook: XSSFWorkbook,
        historyRepository: HistoryRepository
    ) {
        historyRepository.deleteAll()
        workbook.getSheet("History")?.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }
            val lent = row.getCell(0)?.numericCellValue?.toInt() ?: return@forEachIndexed
            val createdAtString = row.getCell(1)?.stringCellValue ?: return@forEachIndexed
            val historyEntity = HistoryEntity(
                id = 0,
                lent = lent,
                createdAt = LocalDateTime.parse(createdAtString, dateFormatter)
            )
            historyRepository.insert(entry = historyEntity)
        }
    }

    private suspend fun importSettingsSheet(
        workbook: XSSFWorkbook,
        settingsRepository: SettingsRepository
    ) {
        workbook.getSheet("Settings")?.getRow(1)?.let { row ->
            val settingsEntity = SettingsEntity(
                id = 0,
                theme = row.getCell(0)?.numericCellValue?.toInt() ?: 0,
                language = row.getCell(1)?.numericCellValue?.toInt() ?: 0
            )
            settingsRepository.get()?.let {
                settingsRepository.delete(settings = it)
            }
            settingsRepository.insert(settingsEntity)
        }
    }

    private suspend fun importAchievementSheet(
        workbook: XSSFWorkbook,
        achievementRepository: AchievementRepository
    ) {
        achievementRepository.deleteAll()
        val sheet = workbook.getSheet("Achievements") ?: return
        val achievements = mutableListOf<AchievementEntity>()
        sheet.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }
            val image = row.getCell(0)?.numericCellValue?.toInt() ?: return@forEachIndexed
            val value = row.getCell(1)?.numericCellValue?.toInt() ?: return@forEachIndexed
            val message = row.getCell(2)?.stringCellValue ?: return@forEachIndexed
            val times = row.getCell(3)?.numericCellValue?.toLong() ?: return@forEachIndexed
            val lastAchievedString = row.getCell(4)?.stringCellValue
            val lastAchieved = if (!lastAchievedString.isNullOrEmpty()) {
                LocalDateTime.parse(lastAchievedString, dateFormatter)
            } else null
            val reset = row.getCell(5)?.booleanCellValue ?: false
            val notify = row.getCell(6)?.booleanCellValue ?: false
            val category = AchievementCategory.valueOf(row.getCell(7)?.stringCellValue ?: "SMOKE_FREE_TIME")
            val unit = AchievementUnit.valueOf(row.getCell(8)?.stringCellValue ?: "DAYS")
            achievements.add(
                AchievementEntity(
                    id = 0,
                    image = image,
                    value = value,
                    message = message,
                    times = times,
                    lastAchieved = lastAchieved,
                    reset = reset,
                    notify = notify,
                    category = category,
                    unit = unit
                )
            )
        }
        achievementRepository.insert(entries = achievements)
    }

    private suspend fun importNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        notificationsSettingsRepository: NotificationsSettingsRepository
    ) {
        val sheet = workbook.getSheet("NotificationsSettings") ?: return
        sheet.getRow(1)?.let { row ->
            val system = row.getCell(0)?.booleanCellValue ?: true
            val achievements = row.getCell(1)?.booleanCellValue ?: true
            val entity = NotificationsSettingsEntity(
                id = 0,
                system = system,
                achievements = achievements
            )
            notificationsSettingsRepository.get()?.let {
                notificationsSettingsRepository.delete(settings = it)
            }
            notificationsSettingsRepository.insert(settings = entity)
        }
    }

    private fun saveWorkbookToFile(
        context: Context,
        workbook: XSSFWorkbook,
        fileName: String
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWorkbookToMediaStore(context, workbook, fileName)
        } else {
            saveWorkbookToLegacyStorage(context, workbook, fileName)
        }.also { workbook.close() }
    }

    private fun saveWorkbookToMediaStore(
        context: Context,
        workbook: XSSFWorkbook,
        fileName: String
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SmokingTracker")
        }
        return context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)?.also { uri ->
            context.contentResolver.openOutputStream(uri)?.use {
                workbook.write(it)
            }
        }
    }

    private fun saveWorkbookToLegacyStorage(
        context: Context,
        workbook: XSSFWorkbook,
        fileName: String
    ): Uri {
        val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, fileName)
        file.outputStream().use {
            workbook.write(it)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    private fun sendNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int,
        notificationsEnabled: Boolean,
        fileUri: Uri? = null
    ) {
        if (context !is MainActivity) {
            return
        }

        if (context.permissionsHelper.isNotificationPermissionGranted() && notificationsEnabled) {
            Notifications.sendNotification(
                context = context,
                title = title,
                content = content,
                notificationId = notificationId,
                fileUri = fileUri
            )
        }
    }
}