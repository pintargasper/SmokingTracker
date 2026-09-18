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
import com.gasperpintar.smokingtracker.utils.manager.Extensions.parseDateTime
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import java.time.format.DateTimeFormatter

object Mappers {

    val HISTORY_HEADERS = listOf("Lent", "CreatedAt")
    val ACHIEVEMENTS_HEADERS = listOf("Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit", "Id")
    val COSTS_HEADERS = listOf("Price", "StartDate", "EndDate")
    val NOTES_HEADERS = listOf("Title", "Content", "Mood", "CreatedAt", "UpdatedAt")
    val SETTINGS_HEADERS = listOf("Theme", "Language", "Frequency", "Currency", "CustomCurrency", "DayEndMinutes")
    val NOTIF_SETTINGS_HEADERS = listOf("System", "Achievements", "Progress")

    fun parseHistory(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): HistoryEntity? {
        val lent = col["Lent"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: return null
        val createdAt = col["CreatedAt"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        return HistoryEntity(id = 0, lent = lent, createdAt = createdAt)
    }

    fun parseAchievement(
        row: Row,
        col: Map<String, Int>,
        index: Int,
        formatter: DateTimeFormatter
    ): AchievementEntity? {
        val enumIndex = index.coerceIn(0, AchievementIcon.entries.lastIndex)
        val lastAchieved = col["LastAchieved"]?.let { columnIndex -> row.parseDateTime(columnIndex, formatter) }

        return runCatching {
            AchievementEntity(
                id = 0,
                image = AchievementIcon.entries[enumIndex].name,
                value = col["Value"]?.let { columnIndex -> row.getCell(columnIndex)?.numericCellValue?.toInt() } ?: 0,
                title = AchievementTitle.entries[enumIndex].name,
                message = AchievementMessage.entries[enumIndex].name,
                times = col["Times"]?.let { columnIndex -> row.getCell(columnIndex)?.numericCellValue?.toLong() } ?: 0L,
                lastAchieved = lastAchieved,
                reset = col["Reset"]?.let { columnIndex -> row.getCell(columnIndex)?.booleanCellValue } ?: false,
                notify = col["Notify"]?.let { columnIndex -> row.getCell(columnIndex)?.booleanCellValue } ?: false,
                category = col["Category"]?.let { columnIndex -> row.getCell(columnIndex)?.stringCellValue }?.let { AchievementCategory.valueOf(it) } ?: return@runCatching null,
                unit = col["Unit"]?.let { columnIndex -> row.getCell(columnIndex)?.stringCellValue }?.let { AchievementUnit.valueOf(it) } ?: return@runCatching null
            )
        }.getOrNull()
    }

    fun parseCost(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): CostEntity? {
        val price = col["Price"]?.let { index -> row.getCell(index)?.numericCellValue } ?: return null
        val startDate = col["StartDate"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        val endDate = col["EndDate"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        return CostEntity(id = 0, price = price, startDate = startDate, endDate = endDate)
    }

    fun parseNote(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): NoteEntity? {
        val title = col["Title"]?.let { index -> row.getCell(index)?.stringCellValue } ?: return null
        val content = col["Content"]?.let { index -> row.getCell(index)?.stringCellValue }.orEmpty()
        val mood = col["Mood"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: return null
        val createdAt = col["CreatedAt"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        val updatedAt = col["UpdatedAt"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        return NoteEntity(id = 0, title = title, content = content, mood = mood, createdAt = createdAt, updatedAt = updatedAt)
    }

    fun parseSettings(
        row: Row,
        col: Map<String, Int>
    ): SettingsEntity {
        val language = col["Language"]?.let { index ->
            row.getCell(index)?.let {
                if (it.cellType == CellType.STRING) it.stringCellValue
                else arrayOf("system", "en", "sl", "uk", "de", "fr", "sr", "sr-Latn", "zh-Hans").getOrNull(it.numericCellValue.toInt())
            }
        } ?: "system"

        return SettingsEntity(
            id = 0,
            theme = col["Theme"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: 0,
            language = language,
            frequency = col["Frequency"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: 0,
            currency = col["Currency"]?.let { index -> row.getCell(index)?.stringCellValue } ?: "€",
            customCurrency = col["CustomCurrency"]?.let { index -> row.getCell(index)?.stringCellValue }.orEmpty(),
            dayEndMinutes = col["DayEndMinutes"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: 0
        )
    }

    fun parseNotificationsSettings(
        row: Row,
        col: Map<String, Int>
    ): NotificationsSettingsEntity {
        return NotificationsSettingsEntity(
            id = 0,
            system = col["System"]?.let { index -> row.getCell(index)?.booleanCellValue } ?: true,
            achievements = col["Achievements"]?.let { index -> row.getCell(index)?.booleanCellValue } ?: true,
            progress = col["Progress"]?.let { index -> row.getCell(index)?.booleanCellValue } ?: true
        )
    }

    fun HistoryEntity.toExcelRow(formatter: DateTimeFormatter): List<Any?> {
        return listOf(lent, createdAt.format(formatter))
    }

    fun AchievementEntity.toExcelRow(formatter: DateTimeFormatter): List<Any?> {
        return listOf(value, times, lastAchieved?.format(formatter).orEmpty(), reset, notify, category.name, unit.name, id)
    }

    fun CostEntity.toExcelRow(formatter: DateTimeFormatter): List<Any?> {
        return listOf(price, startDate.format(formatter), endDate.format(formatter))
    }

    fun NoteEntity.toExcelRow(formatter: DateTimeFormatter): List<Any?> {
        return listOf(title, content, mood, createdAt.format(formatter), updatedAt.format(formatter))
    }

    fun SettingsEntity.toExcelRow(): List<Any?> {
        return listOf(theme, language, frequency, currency, customCurrency, dayEndMinutes)
    }

    fun NotificationsSettingsEntity.toExcelRow(): List<Any?> {
        return listOf(system, achievements, progress)
    }
}