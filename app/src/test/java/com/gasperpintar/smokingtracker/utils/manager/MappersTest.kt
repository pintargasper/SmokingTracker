package com.gasperpintar.smokingtracker.utils.manager

import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.database.model.NotificationsEntry
import com.gasperpintar.smokingtracker.database.model.SettingsEntry
import com.gasperpintar.smokingtracker.model.AchievementJson
import com.gasperpintar.smokingtracker.type.Appearance
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning
import com.gasperpintar.smokingtracker.utils.manager.Extensions.create
import com.gasperpintar.smokingtracker.utils.manager.Extensions.getHeaderColumnMap
import com.gasperpintar.smokingtracker.utils.manager.Extensions.import
import com.gasperpintar.smokingtracker.utils.manager.Mappers.toExcelRow
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MappersTest {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val createdAt = LocalDateTime.of(2026, 1, 1, 12, 30, 15)
    private lateinit var workbook: XSSFWorkbook

    @Before
    fun setup() {
        workbook = XSSFWorkbook()
    }

    @After
    fun teardown() {
        workbook.close()
    }

    @Test
    fun historyRoundTripIsCorrect() {
        val entities = listOf(
            HistoryEntry(id = 7, isLent = true, createdAt = createdAt),
            HistoryEntry(id = 8, isLent = false, createdAt = createdAt.plusHours(1))
        )

        val actual = roundTrip(headers = Mappers.HISTORY_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, _ ->
            Mappers.parseHistory(row, column, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun costRoundTripIsCorrect() {
        val entities = listOf(CostEntry(id = 3, startDate = createdAt, endDate = createdAt.plusDays(30), price = 0.275))

        val actual = roundTrip(headers = Mappers.COSTS_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, _ ->
            Mappers.parseCost(row, column, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun noteRoundTripIsCorrect() {
        val entities = listOf(
            NoteEntry(id = 1, title = "Note ", content = "Content\nin two lines", mood = 5, createdAt = createdAt, updatedAt = createdAt.plusMinutes(5)),
            NoteEntry(id = 2, title = "Empty", content = "", mood = 1, createdAt = createdAt, updatedAt = createdAt)
        )

        val actual = roundTrip(headers = Mappers.NOTES_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, _ ->
            Mappers.parseNote(row, column, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun settingsRoundTripIsCorrect() {
        val entity = SettingsEntry(
            id = 1,
            theme = 2,
            language = "sl",
            frequency = 1, currency = "$",
            customCurrency = "CHF",
            dayEndMinutes = 90,
            elapsedTimeWarningMinutes = 120,
            colorScheme = Appearance.ColorScheme.SYSTEM,
            startPage = Appearance.StartPage.HOME
        )

        val actual = roundTrip(headers = Mappers.SETTINGS_HEADERS, data = listOf(entity), mapper = { it.toExcelRow() }) { row, column, _ ->
            Mappers.parseSettings(row, column)
        }

        assertEquals(listOf(entity.copy(id = 0)), actual)
    }

    @Test
    fun notificationsSettingsRoundTripIsCorrect() {
        val entity = NotificationsEntry(id = 1, system = false, achievements = true, progress = false)

        val actual = roundTrip(headers = Mappers.NOTIF_SETTINGS_HEADERS, data = listOf(entity), mapper = { it.toExcelRow() }) { row, column, _ ->
            Mappers.parseNotificationsSettings(row, column)
        }

        assertEquals(listOf(entity.copy(id = 0)), actual)
    }

    @Test
    fun achievementRoundTripDerivesResourcesFromRowIndex() {
        val entities = listOf(
            achievement(index = 0).copy(id = 1, times = 2, lastAchieved = createdAt, reset = false),
            achievement(index = 1).copy(id = 2)
        )

        val actual = roundTrip(headers = Mappers.ACHIEVEMENTS_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, index ->
            Mappers.parseAchievement(row, column, index, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun parseAchievementClampsIndexToLastResource() {
        val (row, column) = row(
            headers = Mappers.ACHIEVEMENTS_HEADERS,
            values = listOf(1, 0, "", true, true, AchievementJson.Category.CIGARETTES_AVOIDED.name, AchievementJson.Unit.CIGARETTES.name, 1)
        )

        val actual = Mappers.parseAchievement(row, column, index = 100, formatter = formatter)

        assertEquals(AchievementJson.Icon.entries.last().name, actual?.image)
        assertEquals(AchievementJson.Title.entries.last().name, actual?.title)
        assertEquals(AchievementJson.Message.entries.last().name, actual?.message)
    }

    @Test
    fun parseAchievementReturnsNullForInvalidCategory() {
        val (row, column) = row(
            headers = Mappers.ACHIEVEMENTS_HEADERS,
            values = listOf(1, 0, "", true, true, "UNKNOWN", AchievementJson.Unit.DAYS.name, 1)
        )

        assertNull(Mappers.parseAchievement(row, column, index = 0, formatter = formatter))
    }

    @Test
    fun parseAchievementReturnsNullWithoutCategory() {
        val (row, column) = row(headers = listOf("Value", "Unit"), values = listOf(1, AchievementJson.Unit.DAYS.name))

        assertNull(Mappers.parseAchievement(row, column, index = 0, formatter = formatter))
    }

    @Test
    fun parseSettingsMapsLegacyLanguageIndexes() {
        listOf("system", "en", "sl", "uk", "de", "fr", "sr", "sr-Latn", "zh-Hans").forEachIndexed { index, language ->
            val (row, column) = row(headers = listOf("Language"), values = listOf(index))
            assertEquals(language, Mappers.parseSettings(row, column).language)
        }

        val (row, column) = row(headers = listOf("Language"), values = listOf(42))
        assertEquals("system", Mappers.parseSettings(row, column).language)
    }

    @Test
    fun parseSettingsUsesDefaultsForColumnsMissingInOlderBackups() {
        val (row, column) = row(headers = listOf("Theme", "Language", "Frequency", "Currency"), values = listOf(1, "en", 2, "$"))

        val expected = SettingsEntry(
            id = 0, theme = 1,
            language = "en",
            frequency = 2,
            currency = "$",
            customCurrency = "",
            dayEndMinutes = 0,
            elapsedTimeWarningMinutes = 60,
            colorScheme = Appearance.ColorScheme.SYSTEM,
            startPage = Appearance.StartPage.HOME
        )

        assertEquals(expected, Mappers.parseSettings(row, column))
    }

    @Test
    fun parseNotificationsSettingsEnablesMissingColumns() {
        val (row, column) = row(headers = listOf("System"), values = listOf(false))

        val expected = NotificationsEntry(id = 0, system = false, achievements = true, progress = true)

        assertEquals(expected, Mappers.parseNotificationsSettings(row, column))
    }


    @Test
    fun settingsWithoutElapsedTimeWarningColumnPreserveExistingValue() {
        val headers = Mappers.SETTINGS_HEADERS.filterNot {
            it == "ElapsedTimeWarningMinutes"
        }

        val (row, column) = row(
            headers = headers,
            values = listOf(0, "en", 0, "€", "", 0, Appearance.ColorScheme.SYSTEM.name)
        )

        val actual = Mappers.parseSettings(row = row, col = column)

        assertEquals(ElapsedTimeWarning.DEFAULT_MINUTES, actual.elapsedTimeWarningMinutes)
        assertEquals(Appearance.ColorScheme.SYSTEM, actual.colorScheme)
    }

    @Test
    fun settingsWithInvalidElapsedTimeWarningPreserveExistingValue() {
        listOf(-1, 10_081).forEach { value ->
            val (row, column) = row(
                headers = Mappers.SETTINGS_HEADERS,
                values = listOf(0, "en", 0, "€", "", 0, value, Appearance.ColorScheme.SYSTEM.name)
            )

            val actual = Mappers.parseSettings(row = row, col = column)

            assertEquals(ElapsedTimeWarning.DEFAULT_MINUTES, actual.elapsedTimeWarningMinutes)
            assertEquals(Appearance.ColorScheme.SYSTEM, actual.colorScheme)
        }
    }

    @Test
    fun headersMatchExportedColumns() {
        assertEquals(Mappers.HISTORY_HEADERS.size, HistoryEntry(id = 0, isLent = false, createdAt = createdAt).toExcelRow(formatter).size)
        assertEquals(Mappers.ACHIEVEMENTS_HEADERS.size, achievement(index = 0).toExcelRow(formatter).size)
        assertEquals(Mappers.COSTS_HEADERS.size, CostEntry(id = 0, startDate = createdAt, endDate = createdAt, price = 0.0).toExcelRow(formatter).size)
        assertEquals(Mappers.NOTES_HEADERS.size, NoteEntry(id = 0, title = "", content = "", mood = 3, createdAt = createdAt, updatedAt = createdAt).toExcelRow(formatter).size)
        assertEquals(Mappers.SETTINGS_HEADERS.size, SettingsEntry.default().toExcelRow().size)
        assertEquals(Mappers.NOTIF_SETTINGS_HEADERS.size, NotificationsEntry.default().toExcelRow().size)
    }

    private fun <T, R> roundTrip(
        headers: List<String>,
        data: List<T>,
        mapper: (T) -> List<Any?>,
        parser: (Row, Map<String, Int>, Int) -> R?
    ): List<R> {
        workbook.create(name = "Data", headers = headers, data = data, mapper = mapper)
        return workbook.import(sheetName = "Data", requiredHeaders = headers, rowParser = parser)
    }

    private fun row(headers: List<String>, values: List<Any>): Pair<Row, Map<String, Int>> {
        val sheet = workbook.createSheet()
        sheet.createRow(0).apply { headers.forEachIndexed { index, header -> createCell(index).setCellValue(header) } }
        val row = sheet.createRow(1).apply {
            values.forEachIndexed { index, value ->
                when (value) {
                    is Boolean -> createCell(index).setCellValue(value)
                    is Number -> createCell(index).setCellValue(value.toDouble())
                    else -> createCell(index).setCellValue(value.toString())
                }
            }
        }
        return row to sheet.getHeaderColumnMap()!!
    }

    private fun achievement(index: Int): AchievementEntry {
        return AchievementEntry(
            id = 0,
            image = AchievementJson.Icon.entries[index].name,
            value = index + 1,
            title = AchievementJson.Title.entries[index].name,
            message = AchievementJson.Message.entries[index].name,
            times = 0,
            lastAchieved = null,
            reset = true,
            notify = true,
            category = AchievementJson.Category.SMOKE_FREE_TIME,
            unit = AchievementJson.Unit.DAYS
        )
    }
}