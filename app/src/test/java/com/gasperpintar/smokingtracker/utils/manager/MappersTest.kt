package com.gasperpintar.smokingtracker.utils.manager

import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.NoteEntity
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.type.AchievementUnit
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
            HistoryEntity(id = 7, lent = 1, createdAt = createdAt),
            HistoryEntity(id = 8, lent = 0, createdAt = createdAt.plusHours(1))
        )

        val actual = roundTrip(headers = Mappers.HISTORY_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, _ ->
            Mappers.parseHistory(row, column, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun costRoundTripIsCorrect() {
        val entities = listOf(CostEntity(id = 3, startDate = createdAt, endDate = createdAt.plusDays(30), price = 0.275))

        val actual = roundTrip(headers = Mappers.COSTS_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, _ ->
            Mappers.parseCost(row, column, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun noteRoundTripIsCorrect() {
        val entities = listOf(
            NoteEntity(id = 1, title = "Opomba č", content = "Vsebina\nv dveh vrsticah", mood = 5, createdAt = createdAt, updatedAt = createdAt.plusMinutes(5)),
            NoteEntity(id = 2, title = "Empty", content = "", mood = 1, createdAt = createdAt, updatedAt = createdAt)
        )

        val actual = roundTrip(headers = Mappers.NOTES_HEADERS, data = entities, mapper = { it.toExcelRow(formatter) }) { row, column, _ ->
            Mappers.parseNote(row, column, formatter)
        }

        assertEquals(entities.map { it.copy(id = 0) }, actual)
    }

    @Test
    fun settingsRoundTripIsCorrect() {
        val entity = SettingsEntity(id = 1, theme = 2, language = "sl", frequency = 1, currency = "$", customCurrency = "CHF", dayEndMinutes = 90)

        val actual = roundTrip(headers = Mappers.SETTINGS_HEADERS, data = listOf(entity), mapper = { it.toExcelRow() }) { row, column, _ ->
            Mappers.parseSettings(row, column)
        }

        assertEquals(listOf(entity.copy(id = 0)), actual)
    }

    @Test
    fun notificationsSettingsRoundTripIsCorrect() {
        val entity = NotificationsSettingsEntity(id = 1, system = false, achievements = true, progress = false)

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
            values = listOf(1, 0, "", true, true, AchievementCategory.CIGARETTES_AVOIDED.name, AchievementUnit.CIGARETTES.name, 1)
        )

        val actual = Mappers.parseAchievement(row, column, index = 100, formatter = formatter)

        assertEquals(AchievementIcon.entries.last().name, actual?.image)
        assertEquals(AchievementTitle.entries.last().name, actual?.title)
        assertEquals(AchievementMessage.entries.last().name, actual?.message)
    }

    @Test
    fun parseAchievementReturnsNullForInvalidCategory() {
        val (row, column) = row(
            headers = Mappers.ACHIEVEMENTS_HEADERS,
            values = listOf(1, 0, "", true, true, "UNKNOWN", AchievementUnit.DAYS.name, 1)
        )

        assertNull(Mappers.parseAchievement(row, column, index = 0, formatter = formatter))
    }

    @Test
    fun parseAchievementReturnsNullWithoutCategory() {
        val (row, column) = row(headers = listOf("Value", "Unit"), values = listOf(1, AchievementUnit.DAYS.name))

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

        val expected = SettingsEntity(id = 0, theme = 1, language = "en", frequency = 2, currency = "$", customCurrency = "", dayEndMinutes = 0)

        assertEquals(expected, Mappers.parseSettings(row, column))
    }

    @Test
    fun parseNotificationsSettingsEnablesMissingColumns() {
        val (row, column) = row(headers = listOf("System"), values = listOf(false))

        val expected = NotificationsSettingsEntity(id = 0, system = false, achievements = true, progress = true)

        assertEquals(expected, Mappers.parseNotificationsSettings(row, column))
    }

    @Test
    fun headersMatchExportedColumns() {
        assertEquals(Mappers.HISTORY_HEADERS.size, HistoryEntity(id = 0, lent = 0, createdAt = createdAt).toExcelRow(formatter).size)
        assertEquals(Mappers.ACHIEVEMENTS_HEADERS.size, achievement(index = 0).toExcelRow(formatter).size)
        assertEquals(Mappers.COSTS_HEADERS.size, CostEntity(id = 0, startDate = createdAt, endDate = createdAt, price = 0.0).toExcelRow(formatter).size)
        assertEquals(Mappers.NOTES_HEADERS.size, NoteEntity(id = 0, title = "", content = "", mood = 3, createdAt = createdAt, updatedAt = createdAt).toExcelRow(formatter).size)
        assertEquals(Mappers.SETTINGS_HEADERS.size, SettingsEntity.default().toExcelRow().size)
        assertEquals(Mappers.NOTIF_SETTINGS_HEADERS.size, NotificationsSettingsEntity.default().toExcelRow().size)
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

    private fun achievement(index: Int): AchievementEntity {
        return AchievementEntity(
            id = 0,
            image = AchievementIcon.entries[index].name,
            value = index + 1,
            title = AchievementTitle.entries[index].name,
            message = AchievementMessage.entries[index].name,
            times = 0,
            lastAchieved = null,
            reset = true,
            notify = true,
            category = AchievementCategory.SMOKE_FREE_TIME,
            unit = AchievementUnit.DAYS
        )
    }
}