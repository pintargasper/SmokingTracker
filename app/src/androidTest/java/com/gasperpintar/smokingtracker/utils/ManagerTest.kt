package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotesRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
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
import java.time.LocalDateTime

@RunWith(value = AndroidJUnit4::class)
class ManagerTest {

    private val createdAt = LocalDateTime.of(2026, 1, 1, 12, 30, 15)

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
        notificationsRepository =
            NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())
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

            backup(fileUri = uri)

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
            restore(fileUri = uploadWorkbook())

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

    @Test
    fun backupAndRestoreRoundTripPreservesAllData() {
        runBlocking {
            JsonHelper(achievementRepository = achievementRepository).initializeAchievements(context)
            achievementRepository.getAll().first().let {
                achievementRepository.update(entry = it.copy(times = 3, lastAchieved = createdAt, reset = false))
            }
            historyRepository.insertAll(
                entries = listOf(
                    HistoryEntity(id = 0, lent = 0, createdAt = createdAt),
                    HistoryEntity(id = 0, lent = 1, createdAt = createdAt.plusHours(1)),
                    HistoryEntity(id = 0, lent = 0, createdAt = createdAt.plusDays(1))
                )
            )
            costsRepository.insertAll(
                entries = listOf(
                    CostEntity(id = 0, startDate = createdAt.minusDays(30), endDate = createdAt, price = 0.25),
                    CostEntity(id = 0, startDate = createdAt, endDate = createdAt.plusDays(30), price = 0.3)
                )
            )
            notesRepository.insertAll(
                entries = listOf(
                    NoteEntity(id = 0, title = "First", content = "Smoking note", mood = 5, createdAt = createdAt, updatedAt = createdAt.plusMinutes(5)),
                    NoteEntity(id = 0, title = "Second", content = "", mood = 1, createdAt = createdAt.plusDays(1), updatedAt = createdAt.plusDays(1))
                )
            )
            settingsRepository.insert(
                settings = SettingsEntity(id = 0, theme = 2, language = "sl", frequency = 1, currency = "$", customCurrency = "CHF", dayEndMinutes = 90)
            )
            notificationsRepository.insert(
                settings = NotificationsSettingsEntity(id = 0, system = false, achievements = true, progress = false)
            )

            val expected = snapshot()
            val (file, uri) = temporaryFile()

            backup(fileUri = uri)
            clearDatabase()
            restore(fileUri = uri)

            assertEquals(expected, snapshot())
            file.delete()
        }
    }

    @Test
    fun restoreFromInvalidFileKeepsExistingData() {
        runBlocking {
            historyRepository.insert(entry = HistoryEntity(id = 0, lent = 0, createdAt = createdAt))
            val (file, uri) = temporaryFile()
            file.writeText("not a workbook")

            assertTrue(runCatching { restore(fileUri = uri) }.isFailure)
            assertEquals(1, historyRepository.getAll().size)
            file.delete()
        }
    }

    @Test
    fun restoreImportsDateFormattedCells() {
        runBlocking {
            val fileUri = writeWorkbook { workbook ->
                workbook.createSheet("History").apply {
                    createRow(0).apply {
                        Mappers.HISTORY_HEADERS.forEachIndexed { index, header -> createCell(index).setCellValue(header) }
                    }
                    createRow(1).apply {
                        createCell(0).setCellValue(0.0)
                        createCell(1).apply {
                            setCellValue(createdAt)
                            cellStyle = workbook.createCellStyle().apply {
                                dataFormat = workbook.creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
                            }
                        }
                    }
                }
            }

            restore(fileUri = fileUri)

            assertEquals(listOf(createdAt), historyRepository.getAll().map { it.createdAt })
        }
    }

    @Test
    fun restoreWithMalformedSettingsKeepsExistingSettings() {
        runBlocking {
            settingsRepository.insert(settings = SettingsEntity.default(currency = "$"))
            val fileUri = writeWorkbook { workbook ->
                workbook.sheet(name = "History", headers = Mappers.HISTORY_HEADERS, values = listOf(0, "2026-01-01 12:00:00"))
                workbook.sheet(name = "Settings", headers = Mappers.SETTINGS_HEADERS, values = listOf("dark", "en", 0, "€", "", 0))
            }

            restore(fileUri = fileUri)

            assertEquals("$", settingsRepository.get()?.currency)
            assertEquals(1, historyRepository.getAll().size)
        }
    }

    private suspend fun backup(fileUri: Uri) {
        Manager.downloadFile(
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
    }

    private suspend fun restore(fileUri: Uri) {
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
    }

    private suspend fun snapshot(): List<Any?> {
        return listOf(
            historyRepository.getAll().map { it.copy(id = 0) }.sortedBy { it.createdAt },
            achievementRepository.getAll().map { it.copy(id = 0) },
            costsRepository.getAll().map { it.copy(id = 0) },
            notesRepository.getAll().map { it.copy(id = 0) },
            settingsRepository.get()?.copy(id = 0),
            notificationsRepository.get()?.copy(id = 0)
        )
    }

    private suspend fun clearDatabase() {
        historyRepository.deleteAll()
        achievementRepository.deleteAll()
        costsRepository.deleteAll()
        notesRepository.deleteAll()
        settingsRepository.get()?.let { settingsRepository.delete(settings = it) }
        notificationsRepository.get()?.let { notificationsRepository.delete(settings = it) }
    }

    private fun temporaryFile(): Pair<File, Uri> {
        val file = File.createTempFile("backup", ".xlsx", context.cacheDir)
        return file to FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun writeWorkbook(build: (XSSFWorkbook) -> Unit): Uri {
        val file = File.createTempFile("data", ".xlsx", context.cacheDir)

        XSSFWorkbook().use { workbook ->
            build(workbook)
            file.outputStream().use(block = workbook::write)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun uploadWorkbook(): Uri {
        return writeWorkbook { workbook ->
            workbook.sheet(name = "History", headers = Mappers.HISTORY_HEADERS, values = listOf(1, "2026-01-01 12:00:00"))
            workbook.sheet(
                name = "Achievements",
                headers = Mappers.ACHIEVEMENTS_HEADERS,
                values = listOf(9, 2, "", true, true, AchievementCategory.entries.first().name, AchievementUnit.entries.first().name, 1)
            )

            workbook.sheet(name = "Costs", headers = Mappers.COSTS_HEADERS, values = listOf(4.5, "2026-01-01 00:00:00", "2026-01-02 00:00:00"))
            workbook.sheet(name = "Notes", headers = Mappers.NOTES_HEADERS, values = listOf("Test", "Smoking note", 3, "2026-01-01 10:00:00", "2026-01-01 11:00:00"))
            workbook.sheet(name = "Settings", headers = Mappers.SETTINGS_HEADERS, values = listOf(1, 2, 5, "€", ""))
            workbook.sheet(name = "NotificationsSettings", headers = Mappers.NOTIF_SETTINGS_HEADERS, values = listOf(true, true, false))
        }
    }

    private fun XSSFWorkbook.sheet(
        name: String,
        headers: List<String>,
        values: List<Any>
    ) {
        createSheet(name).apply {
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
}