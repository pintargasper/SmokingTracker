package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.gasperpintar.smokingtracker.ui.bar.DataSyncPipeline
import com.gasperpintar.smokingtracker.ui.bar.SyncedStep
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDateTime
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
        onProgress: (Int) -> Unit
    ): Uri = withContext(Dispatchers.IO) {
        XSSFWorkbook().use { workbook ->
            DataSyncPipeline(
                steps = listOf(
                    SyncedStep(weight = 45) { progress -> createHistorySheet(workbook, historyList = historyRepository.getAll(), onStepProgress = progress) },
                    SyncedStep(weight = 30) { progress -> createAchievementSheet(workbook, achievementList = achievementRepository.getAll(), onStepProgress = progress) },
                    SyncedStep(weight = 10) { progress -> createCostsSheet(workbook, costs = costsRepository.getAll(), onStepProgress = progress) },
                    SyncedStep(weight = 5) { progress -> createSettingsSheet(workbook, settingsRepository.get(), onStepProgress = progress) },
                    SyncedStep(weight = 5) { progress -> createNotificationsSettingsSheet(workbook, notificationsSettingsRepository.get(), onStepProgress = progress) },
                    SyncedStep(weight = 5) {
                        progress -> context.contentResolver.openOutputStream(fileUri)?.use {
                            workbook.write(it)
                        }; progress(100)
                    }
                )
            ).run(onProgress)
        }

        sendNotification(
            context,
            title = context.getString(R.string.notification_download_title),
            content = context.getString(R.string.notification_download_content, FileHelper.getFileName(context, fileUri)),
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
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val notificationsEnabled = notificationsSettingsRepository.get()?.system ?: true
        try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                XSSFWorkbook(inputStream).use { workbook ->
                    DataSyncPipeline(
                        steps = listOf(
                            SyncedStep(weight = 45) { progress -> importHistorySheet(workbook, historyRepository, onStepProgress = progress) },
                            SyncedStep(weight = 30) { progress -> importAchievementSheet(workbook, achievementRepository, onStepProgress = progress) },
                            SyncedStep(weight = 10) { progress -> importCostsSheet(workbook, costsRepository, onStepProgress = progress) },
                            SyncedStep(weight = 10) { progress -> importSettingsSheet(workbook, settingsRepository, onStepProgress = progress) },
                            SyncedStep(weight = 5) { progress -> importNotificationsSettingsSheet(workbook, notificationsSettingsRepository, onStepProgress = progress) }
                        )
                    ).run(onProgress)
                }
            }

            sendNotification(
                context,
                title = context.getString(R.string.notification_upload_title),
                content = context.getString(R.string.notification_upload_content),
                notificationId = 1002,
                notificationsEnabled
            )
        } catch (_: Exception) {
            sendNotification(
                context,
                title = context.getString(R.string.notification_upload_failed_title),
                content = context.getString(R.string.notification_upload_failed_content),
                notificationId = 1002,
                notificationsEnabled
            )
        }
    }

    private fun createHistorySheet(
        workbook: XSSFWorkbook,
        historyList: List<HistoryEntity>,
        onStepProgress: (Int) -> Unit = {}
    ) {
        return createSheet(
            workbook,
            name = "History",
            headers = listOf("Lent", "CreatedAt"),
            data = historyList,
            rowMapper = { row, h ->
                row.createCell(0, CellType.NUMERIC).setCellValue(h.lent.toDouble())
                row.createCell(1, CellType.STRING).setCellValue(h.createdAt.format(dateFormatter))
            },
            onStepProgress
        )
    }

    private fun createAchievementSheet(
        workbook: XSSFWorkbook,
        achievementList: List<AchievementEntity>,
        onStepProgress: (Int) -> Unit = {}
    ) {
        return createSheet(
            workbook,
            name = "Achievements",
            headers = listOf("Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit", "Id"),
            data = achievementList,
            rowMapper = { row, achievement ->
                row.createCell(0, CellType.NUMERIC).setCellValue(achievement.value.toDouble())
                row.createCell(1, CellType.NUMERIC).setCellValue(achievement.times.toDouble())
                row.createCell(2, CellType.STRING).setCellValue(achievement.lastAchieved?.format(dateFormatter) ?: "")
                row.createCell(3, CellType.BOOLEAN).setCellValue(achievement.reset)
                row.createCell(4, CellType.BOOLEAN).setCellValue(achievement.notify)
                row.createCell(5, CellType.STRING).setCellValue(achievement.category.name)
                row.createCell(6, CellType.STRING).setCellValue(achievement.unit.name)
                row.createCell(7, CellType.NUMERIC).setCellValue(achievement.id.toDouble())
            },
            onStepProgress
        )
    }

    private fun createSettingsSheet(
        workbook: XSSFWorkbook,
        settings: SettingsEntity?,
        onStepProgress: (Int) -> Unit = {}
    ) {
        createSheet(
            workbook = workbook,
            name = "Settings",
            headers = listOf("Theme", "Language", "Frequency", "Currency", "CustomCurrency"),
            data = if (settings != null) listOf(settings) else emptyList(),
            rowMapper = { row, item ->
                row.createCell(0, CellType.NUMERIC).setCellValue(item.theme.toDouble())
                row.createCell(1, CellType.NUMERIC).setCellValue(item.language.toDouble())
                row.createCell(2, CellType.NUMERIC).setCellValue(item.frequency.toDouble())
                row.createCell(3, CellType.STRING).setCellValue(item.currency)
                row.createCell(4, CellType.STRING).setCellValue(item.customCurrency)
            },
            onStepProgress = onStepProgress
        )
    }

    private fun createNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        notificationsSettings: NotificationsSettingsEntity?,
        onStepProgress: (Int) -> Unit = {}
    ) {
        createSheet(
            workbook = workbook,
            name = "NotificationsSettings",
            headers = listOf("System", "Achievements", "Progress"),
            data = if (notificationsSettings != null) listOf(notificationsSettings) else emptyList(),
            rowMapper = { row, item ->
                row.createCell(0, CellType.BOOLEAN).setCellValue(item.system)
                row.createCell(1, CellType.BOOLEAN).setCellValue(item.achievements)
                row.createCell(2, CellType.BOOLEAN).setCellValue(item.progress)
            },
            onStepProgress = onStepProgress
        )
    }

    private fun createCostsSheet(
        workbook: XSSFWorkbook,
        costs: List<CostEntity>,
        onStepProgress: (Int) -> Unit = {}
    ) {
        createSheet(
            workbook = workbook,
            name = "Costs",
            headers = listOf("Price", "StartDate", "EndDate"),
            data = costs,
            rowMapper = { row, cost ->
                row.createCell(0, CellType.NUMERIC).setCellValue(cost.price)
                row.createCell(1, CellType.STRING).setCellValue(cost.startDate.format(dateFormatter))
                row.createCell(2, CellType.STRING).setCellValue(cost.endDate.format(dateFormatter))
            },
            onStepProgress = onStepProgress
        )
    }

    private fun getColumnIndexMap(
        headerRow: Row
    ): Map<String, Int> {
        return (0 until headerRow.physicalNumberOfCells)
            .mapNotNull { i ->
                headerRow.getCell(i)?.stringCellValue?.trim()?.let {
                    it to i
                }
            }.toMap()
    }

    private suspend fun importHistorySheet(
        workbook: XSSFWorkbook,
        repository: HistoryRepository,
        onStepProgress: (Int) -> Unit = {}
    ) {
        val sheet = workbook.getSheet("History") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val column = getColumnIndexMap(headerRow)

        if (!listOf("Lent", "CreatedAt").all {
            column.containsKey(it)
        }) return

        repository.deleteAll()

        val total = (sheet.physicalNumberOfRows - 1).coerceAtLeast(minimumValue = 1)
        sheet.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }

            val lent = row.getCell(column["Lent"]!!)?.numericCellValue?.toInt() ?: return@forEachIndexed
            val createdAt = row.getCell(column["CreatedAt"]!!)?.stringCellValue?.let {
                LocalDateTime.parse(it, dateFormatter)
            } ?: return@forEachIndexed

            repository.insert(entry = HistoryEntity(id = 0, lent, createdAt))
            onStepProgress((index * 100) / total)
        }

        if (sheet.physicalNumberOfRows <= 1) {
            onStepProgress(100)
        }
    }

    private suspend fun importAchievementSheet(
        workbook: XSSFWorkbook,
        repository: AchievementRepository,
        onStepProgress: (Int) -> Unit = {}
    ) {
        val sheet = workbook.getSheet("Achievements") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val column = getColumnIndexMap(headerRow)

        if (!listOf("Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit").all {
            column.containsKey(it)
        }) return

        repository.deleteAll()

        val entities = mutableListOf<AchievementEntity>()
        val total = (sheet.physicalNumberOfRows - 1).coerceAtLeast(minimumValue = 1)
        sheet.forEachIndexed { index, row ->
            if (index == 0) {
                return@forEachIndexed
            }

            val enumIndex = (index - 1).coerceIn(0, AchievementIcon.entries.size - 1)
            val lastAchieved = row.getCell(column["LastAchieved"]!!)?.stringCellValue?.takeIf {
                it.isNotEmpty()
            }?.let {
                LocalDateTime.parse(it, dateFormatter)
            }

            entities.add(
                AchievementEntity(
                    id = 0,
                    image = AchievementIcon.entries[enumIndex].name,
                    value = row.getCell(column["Value"]!!).numericCellValue.toInt(),
                    title = AchievementTitle.entries[enumIndex].name,
                    message = AchievementMessage.entries[enumIndex].name,
                    times = row.getCell(column["Times"]!!).numericCellValue.toLong(),
                    lastAchieved = lastAchieved,
                    reset = row.getCell(column["Reset"]!!).booleanCellValue,
                    notify = row.getCell(column["Notify"]!!).booleanCellValue,
                    category = AchievementCategory.valueOf(row.getCell(column["Category"]!!).stringCellValue),
                    unit = AchievementUnit.valueOf(row.getCell(column["Unit"]!!).stringCellValue)
                )
            )
            onStepProgress((index * 100) / total)
        }
        repository.insert(entries = entities)

        if (sheet.physicalNumberOfRows <= 1) {
            onStepProgress(100)
        }
    }

    private suspend fun importSettingsSheet(
        workbook: XSSFWorkbook,
        repository: SettingsRepository,
        onStepProgress: (Int) -> Unit = {}
    ) {
        val sheet = workbook.getSheet("Settings") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val column = getColumnIndexMap(headerRow)

        val row = sheet.getRow(1) ?: return
        repository.get()?.let {
            repository.delete(settings = it)
        }

        repository.insert(
            SettingsEntity(
                id = 0,
                theme = getCellValue(column["Theme"], row)?.numericCellValue?.toInt() ?: 0,
                language = getCellValue(column["Language"], row)?.numericCellValue?.toInt() ?: 0,
                frequency = getCellValue(column["Frequency"], row)?.numericCellValue?.toInt() ?: 0,
                currency = getCellValue(column["Currency"], row)?.stringCellValue ?: "€",
                customCurrency = getCellValue(column["CustomCurrency"], row)?.stringCellValue ?: ""
            )
        )
        onStepProgress(100)
    }

    private suspend fun importNotificationsSettingsSheet(
        workbook: XSSFWorkbook,
        repository: NotificationsSettingsRepository,
        onStepProgress: (Int) -> Unit = {}
    ) {
        val sheet = workbook.getSheet("NotificationsSettings") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val column = getColumnIndexMap(headerRow)

        if (!listOf("System", "Achievements", "Progress").all {
            column.containsKey(it)
        }) return

        val row = sheet.getRow(1) ?: return
        repository.get()?.let {
            repository.delete(settings = it)
        }

        repository.insert(
            settings = NotificationsSettingsEntity(
                id = 0,
                system = row.getCell(column["System"]!!)?.booleanCellValue ?: true,
                achievements = row.getCell(column["Achievements"]!!)?.booleanCellValue ?: true,
                progress = row.getCell(column["Progress"]!!)?.booleanCellValue ?: true
            )
        )
        onStepProgress(100)
    }

    private suspend fun importCostsSheet(
        workbook: XSSFWorkbook,
        repository: CostsRepository,
        onStepProgress: (Int) -> Unit = {}
    ) {
        val sheet = workbook.getSheet("Costs") ?: return
        val headerRow = sheet.getRow(0) ?: return
        val column = getColumnIndexMap(headerRow)

        if (!listOf("Price", "StartDate", "EndDate").all {
            column.containsKey(it)
        }) return

        repository.deleteAll()

        val entities = mutableListOf<CostEntity>()
        val total = (sheet.physicalNumberOfRows - 1).coerceAtLeast(minimumValue = 1)

        sheet.forEachIndexed { index, row ->
            if (index == 0) return@forEachIndexed

            val price = row.getCell(column["Price"]!!)?.numericCellValue ?: return@forEachIndexed

            val startDate = row.getCell(column["StartDate"]!!)?.stringCellValue?.let {
                LocalDateTime.parse(it, dateFormatter)
            } ?: return@forEachIndexed

            val endDate = row.getCell(column["EndDate"]!!)?.stringCellValue?.let {
                LocalDateTime.parse(it, dateFormatter)
            } ?: return@forEachIndexed

            entities.add(
                CostEntity(
                    id = 0,
                    price = price,
                    startDate = startDate,
                    endDate = endDate
                )
            )
            onStepProgress((index * 100) / total)
        }
        repository.insertAll(entries = entities)
        if (sheet.physicalNumberOfRows <= 1) {
            onStepProgress(100)
        }
    }

    private fun <T> createSheet(
        workbook: XSSFWorkbook,
        name: String,
        headers: List<String>,
        data: List<T>,
        rowMapper: (row: Row, item: T) -> Unit,
        onStepProgress: (Int) -> Unit = {}
    ) {
        val sheet = workbook.createSheet(name)
        val header = sheet.createRow(0)

        headers.forEachIndexed { index, h ->
            header.createCell(index, CellType.STRING).setCellValue(h)
        }

        val total = data.size.coerceAtLeast(minimumValue = 1)
        data.forEachIndexed { index, item ->
            rowMapper(sheet.createRow(index + 1), item)
            onStepProgress(((index + 1) * 100) / total)
        }

        if (data.isEmpty()) {
            onStepProgress(100)
        }
    }

    private fun getCellValue(columnIndex: Int?, row: Row): Cell? {
        return columnIndex?.takeIf {
            it >= 0
        }?.let {
            row.getCell(it)
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
        if (context is MainActivity && context.permissionsHelper.isNotificationPermissionGranted() && notificationsEnabled) {
            Notifications.sendNotification(context, title, content, notificationId, fileUri)
        }
    }
}