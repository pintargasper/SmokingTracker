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
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
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
            "Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit", "Id"
        )

        headers.forEachIndexed { index, title ->
            header.createCell(index, CellType.STRING).setCellValue(title)
        }

        achievementList.forEachIndexed { index, achievement ->
            val row = sheet.createRow(index + 1)
            row.createCell(0, CellType.NUMERIC).setCellValue(achievement.value.toDouble())
            row.createCell(1, CellType.NUMERIC).setCellValue(achievement.times.toDouble())
            row.createCell(2, CellType.STRING).setCellValue(achievement.lastAchieved?.format(dateFormatter) ?: "")
            row.createCell(3, CellType.BOOLEAN).setCellValue(achievement.reset)
            row.createCell(4, CellType.BOOLEAN).setCellValue(achievement.notify)
            row.createCell(5, CellType.STRING).setCellValue(achievement.category.name)
            row.createCell(6, CellType.STRING).setCellValue(achievement.unit.name)
            row.createCell(7, CellType.NUMERIC).setCellValue(achievement.id.toDouble())
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

        val sheet = workbook.getSheet("History") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val columnIndexMap: Map<String, Int> = (0 until headerRow.physicalNumberOfCells)
            .mapNotNull { index ->
                headerRow.getCell(index)?.stringCellValue?.let {
                    it.trim() to index
                }
            }.toMap()

        val requiredColumns = listOf(
            "Lent", "CreatedAt"
        )
        if (!requiredColumns.all {
            columnIndexMap.containsKey(it)
        }) return

        sheet.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }

            val lentCell = row.getCell(columnIndexMap["Lent"]!!)
            val createdAtCell = row.getCell(columnIndexMap["CreatedAt"]!!)
            val lent = lentCell?.numericCellValue?.toInt() ?: return@forEachIndexed
            val createdAt = createdAtCell?.stringCellValue?.let {
                LocalDateTime.parse(it, dateFormatter)
            } ?: return@forEachIndexed
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
        val sheet = workbook.getSheet("Settings") ?: return
        val headerRow = sheet.getRow(0) ?: return

        val columnIndexMap: Map<String, Int> = (0 until headerRow.physicalNumberOfCells)
            .mapNotNull { index ->
                headerRow.getCell(index)?.stringCellValue?.let { it.trim() to index }
            }.toMap()

        val requiredColumns = listOf(
            "Theme", "Language", "Frequency"
        )
        if (!requiredColumns.all {
            columnIndexMap.containsKey(it)
        }) return

        val row = sheet.getRow(1) ?: return
        repository.get()?.let {
            repository.delete(settings = it)
        }
        repository.insert(
            SettingsEntity(
                id = 0,
                theme = row.getCell(columnIndexMap["Theme"]!!)?.numericCellValue?.toInt() ?: 0,
                language = row.getCell(columnIndexMap["Language"]!!)?.numericCellValue?.toInt() ?: 0,
                frequency = row.getCell(columnIndexMap["Frequency"]!!)?.numericCellValue?.toInt() ?: 0,
            )
        )
    }

    private suspend fun importAchievementSheet(
        workbook: XSSFWorkbook,
        repository: AchievementRepository
    ) {
        val sheet = workbook.getSheet("Achievements") ?: return
        repository.deleteAll()

        val headerRow = sheet.getRow(0) ?: return

        val columnIndexMap: Map<String, Int> = (0 until headerRow.physicalNumberOfCells)
            .mapNotNull { index ->
                headerRow.getCell(index)?.stringCellValue?.let {
                    it.trim() to index
                }
            }.toMap()

        val requiredColumns = listOf(
            "Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit"
        )
        if (!requiredColumns.all {
                columnIndexMap.containsKey(it)
            }) return

        val entities: MutableList<AchievementEntity> = mutableListOf()

        sheet.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }

            val enumIndex: Int = (index - 1).coerceIn(0, AchievementIcon.entries.size - 1)
            val lastAchievedCell = row.getCell(columnIndexMap["LastAchieved"]!!)
            val lastAchieved: LocalDateTime? = lastAchievedCell?.stringCellValue?.takeIf {
                it.isNotEmpty()
            }?.let {
                LocalDateTime.parse(it, dateFormatter)
            }

            entities.add(
                AchievementEntity(
                    id = 0,
                    image = AchievementIcon.entries[enumIndex].name,
                    value = row.getCell(columnIndexMap["Value"]!!).numericCellValue.toInt(),
                    title = AchievementTitle.entries[enumIndex].name,
                    message = AchievementMessage.entries[enumIndex].name,
                    times = row.getCell(columnIndexMap["Times"]!!).numericCellValue.toLong(),
                    lastAchieved = lastAchieved,
                    reset = row.getCell(columnIndexMap["Reset"]!!).booleanCellValue,
                    notify = row.getCell(columnIndexMap["Notify"]!!).booleanCellValue,
                    category = AchievementCategory.valueOf(row.getCell(columnIndexMap["Category"]!!).stringCellValue),
                    unit = AchievementUnit.valueOf(row.getCell(columnIndexMap["Unit"]!!).stringCellValue)
                )
            )
        }
        repository.insert(entries = entities)
    }

    private suspend fun importNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        repository: NotificationsSettingsRepository
    ) {
        val sheet = workbook.getSheet("NotificationsSettings") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val columnIndexMap: Map<String, Int> = (0 until headerRow.physicalNumberOfCells)
            .mapNotNull { index ->
                headerRow.getCell(index)?.stringCellValue?.let { it.trim() to index }
            }.toMap()

        val requiredColumns = listOf(
            "System", "Achievements", "Progress"
        )
        if (!requiredColumns.all {
            columnIndexMap.containsKey(it)
        }) return

        val row = sheet.getRow(1) ?: return
        repository.get()?.let {
            repository.delete(settings = it)
        }
        repository.insert(
            settings = NotificationsSettingsEntity(
                id = 0,
                system = row.getCell(columnIndexMap["System"]!!)?.booleanCellValue ?: true,
                achievements = row.getCell(columnIndexMap["Achievements"]!!)?.booleanCellValue ?: true,
                progress = row.getCell(columnIndexMap["Progress"]!!)?.booleanCellValue ?: true
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