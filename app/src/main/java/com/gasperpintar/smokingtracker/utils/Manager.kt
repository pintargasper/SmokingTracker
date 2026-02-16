package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Manager {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun downloadFile(
        context: Context,
        fileUri: Uri,
        achievementRepository: AchievementRepository,
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        notificationsSettingsRepository: NotificationsSettingsRepository
    ): Uri = withContext(Dispatchers.IO) {
        XSSFWorkbook().use { workbook ->
            createAchievementSheet(workbook, achievementList = achievementRepository.getAll())
            createHistorySheet(workbook, historyList = historyRepository.getAll())
            createSettingsSheet(workbook, settingsRepository.get())
            createNotificationsSettingsSheet(workbook, notificationsSettingsRepository.get())

            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                workbook.write(outputStream)
            }
        }
        sendNotification(
            context = context,
            title = context.getString(R.string.notification_download_title),
            content = context.getString(R.string.notification_download_content, FileHelper.getFileName(context, fileUri)),
            notificationId = 1002,
            fileUri = fileUri,
            notificationsEnabled = notificationsSettingsRepository.get()!!.system
        )
        return@withContext fileUri
    }

    suspend fun uploadFile(
        context: Context,
        fileUri: Uri,
        achievementRepository: AchievementRepository,
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        notificationsSettingsRepository: NotificationsSettingsRepository
    ): Unit = withContext(Dispatchers.IO) {
        val notificationsEnabled: Boolean = notificationsSettingsRepository.get()!!.system
        try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                XSSFWorkbook(inputStream).use { workbook ->

                    importHistorySheet(workbook, historyRepository)
                    importSettingsSheet(workbook, settingsRepository)
                    importAchievementSheet(workbook, achievementRepository)
                    importNotificationsSettingsSheet(workbook, notificationsSettingsRepository)
                }
            }

            sendNotification(
                context = context,
                title = context.getString(R.string.notification_upload_title),
                content = context.getString(R.string.notification_upload_content),
                notificationId = 1002,
                notificationsEnabled = notificationsEnabled
            )
        } catch (_: Exception) {
            sendNotification(
                context = context,
                title = context.getString(R.string.notification_upload_failed_title),
                content = context.getString(R.string.notification_upload_failed_content),
                notificationId = 1002,
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    private fun createHistorySheet(workbook: XSSFWorkbook, historyList: List<HistoryEntity>) {
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

    private fun createSettingsSheet(workbook: XSSFWorkbook, settings: SettingsEntity?) {
        val sheet = workbook.createSheet("Settings")
        val header = sheet.createRow(0)

        header.createCell(0, CellType.STRING).setCellValue("Theme")
        header.createCell(1, CellType.STRING).setCellValue("Language")
        header.createCell(2, CellType.STRING).setCellValue("Frequency")

        settings?.let {
            val row = sheet.createRow(1)
            row.createCell(0, CellType.NUMERIC).setCellValue(it.theme.toDouble())
            row.createCell(1, CellType.NUMERIC).setCellValue(it.language.toDouble())
            row.createCell(2, CellType.NUMERIC).setCellValue(it.frequency.toDouble())
        }
    }

    private fun createAchievementSheet(workbook: XSSFWorkbook, achievementList: List<AchievementEntity>) {
        val sheet = workbook.createSheet("Achievements")
        val header = sheet.createRow(0)

        val headers: List<String> = listOf(
            "Image","Value","Title","Message","Times",
            "LastAchieved","Reset","Notify","Category","Unit"
        )

        headers.forEachIndexed { index, title ->
            header.createCell(index, CellType.STRING).setCellValue(title)
        }

        achievementList.forEachIndexed { index, achievement ->
            val row = sheet.createRow(index + 1)
            row.createCell(0, CellType.NUMERIC).setCellValue(achievement.image.toDouble())
            row.createCell(1, CellType.NUMERIC).setCellValue(achievement.value.toDouble())
            row.createCell(2, CellType.NUMERIC).setCellValue(achievement.title.toDouble())
            row.createCell(3, CellType.NUMERIC).setCellValue(achievement.message.toDouble())
            row.createCell(4, CellType.NUMERIC).setCellValue(achievement.times.toDouble())
            row.createCell(5, CellType.STRING).setCellValue(achievement.lastAchieved?.format(dateFormatter) ?: "")
            row.createCell(6, CellType.BOOLEAN).setCellValue(achievement.reset)
            row.createCell(7, CellType.BOOLEAN).setCellValue(achievement.notify)
            row.createCell(8, CellType.STRING).setCellValue(achievement.category.name)
            row.createCell(9, CellType.STRING).setCellValue(achievement.unit.name)
        }
    }

    private fun createNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        notificationsSettings: NotificationsSettingsEntity?
    ) {
        val sheet = workbook.createSheet("NotificationsSettings")
        val header = sheet.createRow(0)

        header.createCell(0, CellType.STRING).setCellValue("System")
        header.createCell(1, CellType.STRING).setCellValue("Achievements")
        header.createCell(2, CellType.STRING).setCellValue("Progress")

        notificationsSettings?.let {
            val row = sheet.createRow(1)
            row.createCell(0, CellType.BOOLEAN).setCellValue(it.system)
            row.createCell(1, CellType.BOOLEAN).setCellValue(it.achievements)
            row.createCell(2, CellType.BOOLEAN).setCellValue(it.progress)
        }
    }

    private suspend fun importHistorySheet(workbook: XSSFWorkbook, repository: HistoryRepository) {
        repository.deleteAll()
        workbook.getSheet("History")?.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }
            val lent = row.getCell(0)?.numericCellValue?.toInt() ?: return@forEachIndexed
            val createdAt = LocalDateTime.parse(row.getCell(1).stringCellValue, dateFormatter)
            repository.insert(
                entry = HistoryEntity(
                    id = 0,
                    lent = lent,
                    createdAt = createdAt
                )
            )
        }
    }

    private suspend fun importSettingsSheet(workbook: XSSFWorkbook, repository: SettingsRepository) {
        workbook.getSheet("Settings")?.getRow(1)?.let { row ->
            repository.get()?.let {
                repository.delete(settings = it)
            }

            repository.insert(
                SettingsEntity(
                    id = 0,
                    theme = row.getCell(0).numericCellValue.toInt(),
                    language = row.getCell(1).numericCellValue.toInt(),
                    frequency = row.getCell(2)?.numericCellValue?.toInt() ?: 0,
                )
            )
        }
    }

    private suspend fun importAchievementSheet(workbook: XSSFWorkbook, repository: AchievementRepository) {
        val sheet = workbook.getSheet("Achievements") ?: return
        repository.deleteAll()

        val entities: MutableList<AchievementEntity> = mutableListOf()

        sheet.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }
            entities.add(
                AchievementEntity(
                    id = 0,
                    image = row.getCell(0).numericCellValue.toInt(),
                    value = row.getCell(1).numericCellValue.toInt(),
                    title = row.getCell(2).numericCellValue.toInt(),
                    message = row.getCell(3).numericCellValue.toInt(),
                    times = row.getCell(4).numericCellValue.toLong(),
                    lastAchieved = row.getCell(5).stringCellValue.takeIf {
                        it.isNotEmpty()
                    }?.let {
                        LocalDateTime.parse(it, dateFormatter)
                    },
                    reset = row.getCell(6).booleanCellValue,
                    notify = row.getCell(7).booleanCellValue,
                    category = AchievementCategory.valueOf(row.getCell(8).stringCellValue),
                    unit = AchievementUnit.valueOf(row.getCell(9).stringCellValue)
                )
            )
        }
        repository.insert(entries = entities)
    }

    private suspend fun importNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        repository: NotificationsSettingsRepository
    ) {
        val row = workbook.getSheet("NotificationsSettings")?.getRow(1) ?: return

        repository.get()?.let {
            repository.delete(settings = it)
        }

        repository.insert(
            settings = NotificationsSettingsEntity(
                id = 0,
                system = row.getCell(0).booleanCellValue,
                achievements = row.getCell(1).booleanCellValue,
                progress = row.getCell(2)?.booleanCellValue ?: true
            )
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
            Notifications.sendNotification(context, title, content, notificationId, fileUri)
        }
    }
}