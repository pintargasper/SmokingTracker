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
import org.apache.poi.ss.usermodel.Row
import java.time.format.DateTimeFormatter

object Mappers {

    val HISTORY_HEADERS = listOf("Lent", "CreatedAt")
    val ACHIEVEMENTS_HEADERS = listOf("Value", "Times", "LastAchieved", "Reset", "Notify", "Category", "Unit", "Id")
    val COSTS_HEADERS = listOf("Price", "StartDate", "EndDate")
    val NOTES_HEADERS = listOf("Title", "Content", "Mood", "CreatedAt", "UpdatedAt")
    val SETTINGS_HEADERS = listOf("Theme", "Language", "Frequency", "Currency", "CustomCurrency")
    val NOTIF_SETTINGS_HEADERS = listOf("System", "Achievements", "Progress")

    fun parseHistory(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): HistoryEntity? {
        val lent = row.getCell(col["Lent"]!!)?.numericCellValue?.toInt() ?: return null
        val createdAt = row.parseDateTime(col["CreatedAt"]!!, formatter) ?: return null
        return HistoryEntity(id = 0, lent = lent, createdAt = createdAt)
    }

    fun parseAchievement(
        row: Row,
        col: Map<String, Int>,
        index: Int,
        formatter: DateTimeFormatter
    ): AchievementEntity? {
        val enumIndex = index.coerceIn(0, AchievementIcon.entries.lastIndex)
        val lastAchieved = row.parseDateTime(col["LastAchieved"]!!, formatter)

        return runCatching {
            AchievementEntity(
                id = 0,
                image = AchievementIcon.entries[enumIndex].name,
                value = row.getCell(col["Value"]!!)?.numericCellValue?.toInt() ?: 0,
                title = AchievementTitle.entries[enumIndex].name,
                message = AchievementMessage.entries[enumIndex].name,
                times = row.getCell(col["Times"]!!)?.numericCellValue?.toLong() ?: 0L,
                lastAchieved = lastAchieved,
                reset = row.getCell(col["Reset"]!!)?.booleanCellValue ?: false,
                notify = row.getCell(col["Notify"]!!)?.booleanCellValue ?: false,
                category = AchievementCategory.valueOf(row.getCell(col["Category"]!!)?.stringCellValue.orEmpty()),
                unit = AchievementUnit.valueOf(row.getCell(col["Unit"]!!)?.stringCellValue.orEmpty())
            )
        }.getOrNull()
    }

    fun parseCost(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): CostEntity? {
        val price = row.getCell(col["Price"]!!)?.numericCellValue ?: return null
        val startDate = row.parseDateTime(col["StartDate"]!!, formatter) ?: return null
        val endDate = row.parseDateTime(col["EndDate"]!!, formatter) ?: return null
        return CostEntity(id = 0, price = price, startDate = startDate, endDate = endDate)
    }

    fun parseNote(
        row: Row,
        col: Map<String, Int>,
        formatter: DateTimeFormatter
    ): NoteEntity? {
        val title = row.getCell(col["Title"]!!)?.stringCellValue ?: return null
        val content = row.getCell(col["Content"]!!)?.stringCellValue.orEmpty()
        val mood = row.getCell(col["Mood"]!!)?.numericCellValue?.toInt() ?: return null
        val createdAt = row.parseDateTime(col["CreatedAt"]!!, formatter) ?: return null
        val updatedAt = row.parseDateTime(col["UpdatedAt"]!!, formatter) ?: return null
        return NoteEntity(id = 0, title = title, content = content, mood = mood, createdAt = createdAt, updatedAt = updatedAt)
    }

    fun parseSettings(
        row: Row,
        col: Map<String, Int>
    ): SettingsEntity {
        return SettingsEntity(
            id = 0,
            theme = row.getCell(col["Theme"]!!)?.numericCellValue?.toInt() ?: 0,
            language = row.getCell(col["Language"]!!)?.numericCellValue?.toInt() ?: 0,
            frequency = row.getCell(col["Frequency"]!!)?.numericCellValue?.toInt() ?: 0,
            currency = row.getCell(col["Currency"]!!)?.stringCellValue ?: "€",
            customCurrency = row.getCell(col["CustomCurrency"]!!)?.stringCellValue.orEmpty()
        )
    }

    fun parseNotificationsSettings(
        row: Row,
        col: Map<String, Int>
    ): NotificationsSettingsEntity {
        return NotificationsSettingsEntity(
            id = 0,
            system = row.getCell(col["System"]!!)?.booleanCellValue ?: true,
            achievements = row.getCell(col["Achievements"]!!)?.booleanCellValue ?: true,
            progress = row.getCell(col["Progress"]!!)?.booleanCellValue ?: true
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
        return listOf(theme, language, frequency, currency, customCurrency)
    }

    fun NotificationsSettingsEntity.toExcelRow(): List<Any?> {
        return listOf(system, achievements, progress)
    }
}