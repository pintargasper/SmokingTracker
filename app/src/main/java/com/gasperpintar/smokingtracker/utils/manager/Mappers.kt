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
import com.gasperpintar.smokingtracker.utils.manager.Extensions.parseDateTime
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import java.time.format.DateTimeFormatter

object Mappers {

    val HISTORY_HEADERS = listOf("Lent", "CreatedAt")
    val ACHIEVEMENTS_HEADERS = listOf("Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit", "Id")
    val COSTS_HEADERS = listOf("Price", "StartDate", "EndDate")
    val NOTES_HEADERS = listOf("Title", "Content", "Mood", "CreatedAt", "UpdatedAt")
    val SETTINGS_HEADERS = listOf("Theme", "Language", "Frequency", "Currency", "CustomCurrency", "DayEndMinutes", "ElapsedTimeWarningMinutes", "ColorScheme", "StartPage")
    val NOTIF_SETTINGS_HEADERS = listOf("System", "Achievements", "Progress")

    fun parseHistory(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): HistoryEntry? {
        val lent = col["Lent"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: return null
        val createdAt = col["CreatedAt"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        return HistoryEntry(id = 0, isLent = lent > 0, createdAt = createdAt, timerLabel = "")
    }

    fun parseAchievement(
        row: Row,
        col: Map<String, Int>,
        index: Int,
        formatter: DateTimeFormatter
    ): AchievementEntry? {
        val enumIndex = index.coerceIn(0, AchievementJson.Icon.entries.lastIndex)
        val lastAchieved = col["LastAchieved"]?.let { columnIndex -> row.parseDateTime(columnIndex, formatter) }

        return runCatching {
            AchievementEntry(
                id = 0,
                image = AchievementJson.Icon.entries[enumIndex].name,
                value = col["Value"]?.let { columnIndex -> row.getCell(columnIndex)?.numericCellValue?.toInt() } ?: 0,
                title = AchievementJson.Title.entries[enumIndex].name,
                message = AchievementJson.Message.entries[enumIndex].name,
                times = col["Times"]?.let { columnIndex -> row.getCell(columnIndex)?.numericCellValue?.toLong() } ?: 0L,
                lastAchieved = lastAchieved,
                reset = col["Reset"]?.let { columnIndex -> row.getCell(columnIndex)?.booleanCellValue } ?: false,
                notify = col["Notify"]?.let { columnIndex -> row.getCell(columnIndex)?.booleanCellValue } ?: false,
                category = col["Category"]?.let { columnIndex -> row.getCell(columnIndex)?.stringCellValue }?.let { AchievementJson.Category.valueOf(it) } ?: return@runCatching null,
                unit = col["Unit"]?.let { columnIndex -> row.getCell(columnIndex)?.stringCellValue }?.let { AchievementJson.Unit.valueOf(it) } ?: return@runCatching null
            )
        }.getOrNull()
    }

    fun parseCost(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): CostEntry? {
        val price = col["Price"]?.let { index -> row.getCell(index)?.numericCellValue } ?: return null
        val startDate = col["StartDate"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        val endDate = col["EndDate"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        return CostEntry(id = 0, price = price, startDate = startDate, endDate = endDate)
    }

    fun parseNote(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): NoteEntry? {
        val title = col["Title"]?.let { index -> row.getCell(index)?.stringCellValue } ?: return null
        val content = col["Content"]?.let { index -> row.getCell(index)?.stringCellValue }.orEmpty()
        val mood = col["Mood"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: return null
        val createdAt = col["CreatedAt"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        val updatedAt = col["UpdatedAt"]?.let { index -> row.parseDateTime(index, formatter) } ?: return null
        return NoteEntry(id = 0, title = title, content = content, mood = mood, createdAt = createdAt, updatedAt = updatedAt)
    }

    fun parseSettings(
        row: Row,
        col: Map<String, Int>
    ): SettingsEntry {
        val language = col["Language"]?.let { index ->
            row.getCell(index)?.let {
                if (it.cellType == CellType.STRING) it.stringCellValue
                else arrayOf("system", "en", "sl", "uk", "de", "fr", "sr", "sr-Latn", "zh-Hans").getOrNull(it.numericCellValue.toInt())
            }
        } ?: "system"

        val warningMinutes = col["ElapsedTimeWarningMinutes"]?.let { index ->
            row.getCell(index)?.numericCellValue?.toInt()
        }?.takeIf(predicate = ElapsedTimeWarning::isValidMinutes)
            ?: ElapsedTimeWarning.DEFAULT_MINUTES


        return SettingsEntry(
            id = 0,
            theme = col["Theme"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: 0,
            language = language,
            frequency = col["Frequency"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: 0,
            currency = col["Currency"]?.let { index -> row.getCell(index)?.stringCellValue } ?: "€",
            customCurrency = col["CustomCurrency"]?.let { index -> row.getCell(index)?.stringCellValue }.orEmpty(),
            dayEndMinutes = col["DayEndMinutes"]?.let { index -> row.getCell(index)?.numericCellValue?.toInt() } ?: 0,
            elapsedTimeWarningMinutes = warningMinutes,
            colorScheme = col["ColorScheme"]?.let { index -> row.getCell(index)?.stringCellValue }?.let { Appearance.ColorScheme.valueOf(it) } ?: Appearance.ColorScheme.SYSTEM,
            startPage = col["StartPage"]?.let { index -> row.getCell(index)?.stringCellValue }?.let { Appearance.StartPage.valueOf(it) } ?: Appearance.StartPage.HOME
        )
    }

    fun parseNotificationsSettings(
        row: Row,
        col: Map<String, Int>
    ): NotificationsEntry {
        return NotificationsEntry(
            id = 0,
            system = col["System"]?.let { index -> row.getCell(index)?.booleanCellValue } ?: true,
            achievements = col["Achievements"]?.let { index -> row.getCell(index)?.booleanCellValue } ?: true,
            progress = col["Progress"]?.let { index -> row.getCell(index)?.booleanCellValue } ?: true
        )
    }

    fun HistoryEntry.toExcelRow(
        formatter: DateTimeFormatter
    ): List<Any?> {
        return listOf(if (isLent) 1 else 0, createdAt.format(formatter))
    }

    fun AchievementEntry.toExcelRow(
        formatter: DateTimeFormatter
    ): List<Any?> {
        return listOf(value, times, lastAchieved?.format(formatter).orEmpty(), reset, notify, category.name, unit.name, id)
    }

    fun CostEntry.toExcelRow(
        formatter: DateTimeFormatter
    ): List<Any?> {
        return listOf(price, startDate.format(formatter), endDate.format(formatter))
    }

    fun NoteEntry.toExcelRow(
        formatter: DateTimeFormatter
    ): List<Any?> {
        return listOf(title, content, mood, createdAt.format(formatter), updatedAt.format(formatter))
    }

    fun SettingsEntry.toExcelRow(): List<Any?> {
        return listOf(theme, language, frequency, currency, customCurrency, dayEndMinutes, elapsedTimeWarningMinutes, colorScheme, startPage)
    }

    fun NotificationsEntry.toExcelRow(): List<Any?> {
        return listOf(system, achievements, progress)
    }
}