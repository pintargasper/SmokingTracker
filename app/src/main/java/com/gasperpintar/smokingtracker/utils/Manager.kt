package com.gasperpintar.smokingtracker.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
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
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
        fileName: String = "st_data.xlsx"
    ): Uri? = withContext(context = Dispatchers.IO) {
        XSSFWorkbook().use { workbook ->
            createHistorySheet(workbook, historyList = historyRepository.getAll())
            createSettingsSheet(workbook, settings = settingsRepository.get())
            val fileUri = saveWorkbookToFile(context, workbook, fileName)
            sendNotification(
                context,
                title = context.getString(R.string.notification_download_title),
                content = context.getString(
                    R.string.notification_download_content,
                    fileName
                ),
                notificationId = 1002,
                fileUri = fileUri
            )
            return@withContext fileUri
        }
    }

    suspend fun uploadFile(context: Context, 
                           fileUri: Uri, 
                           historyRepository: HistoryRepository,
                           settingsRepository: SettingsRepository
    ) = withContext(
        context = Dispatchers.IO
    ) {
        try {
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                XSSFWorkbook(inputStream).use { workbook ->
                    importHistorySheet(workbook, historyRepository = historyRepository)
                    importSettingsSheet(workbook, settingsRepository = settingsRepository)
                }
            }
            sendNotification(
                context,
                title = context.getString(R.string.notification_upload_title),
                content = context.getString(R.string.notification_upload_content),
                notificationId = 1002,
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            sendNotification(
                context,
                title = context.getString(R.string.notification_upload_failed_title),
                content = context.getString(R.string.notification_upload_failed_content),
                notificationId = 1002,
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
            settingsRepository.update(settingsEntity)
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
            saveWorkbookToLegacyStorage(workbook, fileName)
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
            context.contentResolver.openOutputStream(uri)?.use { workbook.write(it) }
        }
    }

    private fun saveWorkbookToLegacyStorage(
        workbook: XSSFWorkbook,
        fileName: String
    ): Uri {
        val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, fileName)
        file.outputStream().use { workbook.write(it) }
        return Uri.fromFile(file)
    }

    private fun sendNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int,
        fileUri: Uri? = null
    ) {
        if (context !is MainActivity) {
            return
        }

        if (context.permissionsHelper.isNotificationPermissionGranted()) {
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