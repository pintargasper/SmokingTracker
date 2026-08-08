package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.repository.*
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

@RunWith(value = AndroidJUnit4::class)
class ManagerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    private lateinit var achievementRepository: AchievementRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notificationsRepository: NotificationsSettingsRepository
    private lateinit var costsRepository: CostsRepository
    private lateinit var notesRepository: NotesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = TestProvider.getInMemoryDatabase(context)
        achievementRepository = AchievementRepository(achievementDao = database.achievementDao())
        historyRepository = HistoryRepository(historyDao = database.historyDao())
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        notificationsRepository = NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())
        costsRepository = CostsRepository(costDao = database.costsDao())
        notesRepository = NotesRepository(notesDao = database.notesDao())
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun backupDataToWorkbook() {
        runBlocking {
            val (file, uri) = temporaryFile()

            Manager.downloadFile(
                context = context,
                fileUri = uri,
                achievementRepository = achievementRepository,
                historyRepository = historyRepository,
                settingsRepository = settingsRepository,
                notificationsSettingsRepository = notificationsRepository,
                costsRepository = costsRepository,
                notesRepository = notesRepository,
                onProgress = {}
            )

            assertTrue(file.exists())
            assertTrue(file.length() > 0)

            FileInputStream(file).use { input ->
                XSSFWorkbook(input).use { workbook ->
                    assertTrue(workbook.numberOfSheets > 0)
                }
            }
            file.delete()
        }
    }

    @Test
    fun restoreDataFromWorkbook() {
        runBlocking {
            val fileUri = uploadWorkbook()

            Manager.uploadFile(
                context = context,
                fileUri = fileUri,
                achievementRepository = achievementRepository,
                historyRepository = historyRepository,
                settingsRepository = settingsRepository,
                notificationsSettingsRepository = notificationsRepository,
                costsRepository = costsRepository,
                notesRepository = notesRepository,
                onProgress = {}
            )

            val history = historyRepository.getAll().first()
            assertTrue(history.lent == 1)

            val achievement = achievementRepository.getAll().first()
            assertTrue(achievement.value == 9)
            assertTrue(achievement.times == 2L)
            assertTrue(achievement.notify)

            val cost = costsRepository.getAll().first()
            assertTrue(cost.price == 4.5)

            val note = notesRepository.getAll().first()
            assertTrue(note.title == "Test")
            assertTrue(note.content == "Smoking note")
            assertTrue(note.mood == 3)

            val settings = settingsRepository.get()
            assertTrue(settings?.currency == "€")
            assertTrue(settings?.frequency == 5)

            val notifications = notificationsRepository.get()
            assertTrue(notifications?.system == true)
            assertTrue(notifications?.progress == false)
        }
    }


    private fun temporaryFile(): Pair<File, Uri> {
        val file = File.createTempFile("backup", ".xlsx", context.cacheDir)
        return file to FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    private fun uploadWorkbook(): Uri {
        val file = File.createTempFile("data", ".xlsx", context.cacheDir)

        XSSFWorkbook().use { workbook ->

            fun sheet(
                name: String,
                headers: List<String>,
                values: List<Any>
            ) {
                workbook.createSheet(name).apply {
                    createRow(0).apply {
                        headers.forEachIndexed { index, h ->
                            createCell(index).setCellValue(h)
                        }
                    }

                    createRow(1).apply {
                        values.forEachIndexed { index, value ->
                            when (value) {
                                is Boolean -> createCell(index).setCellValue(value)
                                is Number -> createCell(index).setCellValue(value.toDouble())
                                else -> createCell(index).setCellValue(value.toString())
                            }
                        }
                    }
                }
            }

            sheet(
                name = "History",
                headers = listOf("Lent", "CreatedAt"),
                values = listOf(1, "2026-01-01 12:00:00")
            )

            sheet(
                name = "Achievements",
                headers = listOf("Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit"),
                values = listOf(9, 2, "", true, true, AchievementCategory.entries.first().name, AchievementUnit.entries.first().name)
            )

            sheet(
                name = "Costs",
                headers = listOf("Price", "StartDate", "EndDate"),
                values = listOf(4.5, "2026-01-01 00:00:00", "2026-01-02 00:00:00")
            )

            sheet(
                name = "Notes",
                headers = listOf("Title", "Content", "Mood", "CreatedAt", "UpdatedAt"),
                values = listOf("Test", "Smoking note", 3, "2026-01-01 10:00:00", "2026-01-01 11:00:00")
            )

            sheet(
                name = "Settings",
                headers = listOf("Theme", "Language", "Frequency", "Currency", "CustomCurrency"),
                values = listOf(1, 2, 5, "€", "")
            )

            sheet(
                name = "NotificationsSettings",
                headers = listOf("System", "Achievements", "Progress"),
                values = listOf(true, true, false)
            )
            file.outputStream().use(block = workbook::write)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}