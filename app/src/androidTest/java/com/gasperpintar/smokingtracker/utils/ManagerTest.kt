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
import com.gasperpintar.smokingtracker.utils.manager.Manager
import com.gasperpintar.smokingtracker.utils.manager.Mappers
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
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

            FileInputStream(file).use { XSSFWorkbook(it).use { workbook ->
                assertTrue(workbook.numberOfSheets > 0)
            }}
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

            val histories = historyRepository.getAll()
            val achievements = achievementRepository.getAll()
            val costs = costsRepository.getAll()
            val notes = notesRepository.getAll()
            val settings = settingsRepository.get()
            val notifications = notificationsRepository.get()

            assertTrue("History should not be empty", histories.isNotEmpty())
            assertTrue("Achievements should not be empty", achievements.isNotEmpty())
            assertTrue("Costs should not be empty", costs.isNotEmpty())
            assertTrue("Notes should not be empty", notes.isNotEmpty())
            assertTrue("Settings should exist", settings != null)
            assertTrue("Notifications settings should exist", notifications != null)

            assertEquals(1, histories.first().lent)

            val achievement = achievements.first()
            assertEquals(9, achievement.value)
            assertEquals(2L, achievement.times)
            assertEquals(true, achievement.notify)

            assertEquals(4.5, costs.first().price, 0.001)

            val note = notes.first()
            assertEquals("Test", note.title)
            assertEquals("Smoking note", note.content)
            assertEquals(3, note.mood)

            assertEquals("€", settings?.currency)
            assertEquals(5, settings?.frequency)

            assertEquals(true, notifications?.system)
            assertEquals(false, notifications?.progress)
        }
    }

    private fun temporaryFile(): Pair<File, Uri> {
        val file = File.createTempFile("backup", ".xlsx", context.cacheDir)
        return file to FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
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
                        headers.forEachIndexed { index, value -> createCell(index).setCellValue(value) }
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

            sheet(name = "History", headers = Mappers.HISTORY_HEADERS, values = listOf(1, "2026-01-01 12:00:00"))
            sheet(
                name = "Achievements",
                headers = Mappers.ACHIEVEMENTS_HEADERS,
                values = listOf(9, 2, "", true, true, AchievementCategory.entries.first().name, AchievementUnit.entries.first().name, 1)
            )

            sheet(name = "Costs", headers = Mappers.COSTS_HEADERS, values = listOf(4.5, "2026-01-01 00:00:00", "2026-01-02 00:00:00"))
            sheet(name = "Notes", headers = Mappers.NOTES_HEADERS, values = listOf("Test", "Smoking note", 3, "2026-01-01 10:00:00", "2026-01-01 11:00:00"))
            sheet(name = "Settings", headers = Mappers.SETTINGS_HEADERS, values = listOf(1, 2, 5, "€", ""))
            sheet(name = "NotificationsSettings", headers = Mappers.NOTIF_SETTINGS_HEADERS, values = listOf(true, true, false))
            file.outputStream().use(block = workbook::write)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}