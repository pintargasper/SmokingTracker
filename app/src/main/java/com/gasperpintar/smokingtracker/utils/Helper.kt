package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.model.HistoryEntry
import com.gasperpintar.smokingtracker.type.AchievementUnit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter

object Helper {

    fun getDay(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        return date.atStartOfDay() to date.atTime(LocalTime.MAX)
    }

    fun getWeek(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val startOfWeek: LocalDateTime = date.with(DayOfWeek.MONDAY).atStartOfDay()
        val endOfWeek: LocalDateTime = startOfWeek.plusDays(6).withHour(23).withMinute(59).withSecond(59).withNano(999_999_999)
        return startOfWeek to endOfWeek
    }

    fun getMonth(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val startOfMonth = date.withDayOfMonth(1).atStartOfDay()
        val endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX)
        return startOfMonth to endOfMonth
    }

    fun getYear(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        val startOfYear: LocalDateTime = date.withDayOfYear(1).atStartOfDay()
        val endOfYear: LocalDateTime = date.withDayOfYear(date.lengthOfYear()).atTime(LocalTime.MAX)
        return startOfYear to endOfYear
    }

    fun getEndOfDay(date: LocalDate): LocalDateTime {
        return date.atTime(LocalTime.MAX)
    }

    @Deprecated(
        message = "Since version 1.3.0. Use getEndOfDay(date: LocalDate) instead",
        replaceWith = ReplaceWith(expression = "getEndOfDay(date)")
    )
    fun getEndOfDay(): LocalDateTime {
        return LocalDateTime.now().withHour(23)
            .withMinute(59).withSecond(59)
            .withNano(999_999_999)
    }

    fun HistoryEntity.toHistoryEntry(): HistoryEntry {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return HistoryEntry(
            id = id,
            isLent = lent > 0,
            createdAt = createdAt,
            timerLabel = createdAt.format(timeFormatter)
        )
    }

    fun HistoryEntry.toHistoryEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            createdAt = createdAt,
            lent = if (isLent) {
                1
            } else {
                0
            }
        )
    }

    fun AchievementEntity.toAchievementEntry(): AchievementEntry {
        return AchievementEntry(
            id = id,
            image = image,
            value = value,
            message = message,
            times = times,
            lastAchieved = lastAchieved,
            reset = reset,
            notify = notify,
            category = category,
            unit = unit
        )
    }

    fun AchievementEntry.toAchievementEntity(): AchievementEntity {
        return AchievementEntity(
            id = id,
            image = image,
            value = value,
            message = message,
            times = times,
            lastAchieved = lastAchieved,
            reset = reset,
            notify = notify,
            category = category,
            unit = unit
        )
    }

    fun Context.getDayOfWeekName(dayOfWeek: DayOfWeek): String {
        return when(dayOfWeek) {
            DayOfWeek.MONDAY -> getString(R.string.day_monday)
            DayOfWeek.TUESDAY -> getString(R.string.day_tuesday)
            DayOfWeek.WEDNESDAY -> getString(R.string.day_wednesday)
            DayOfWeek.THURSDAY -> getString(R.string.day_thursday)
            DayOfWeek.FRIDAY -> getString(R.string.day_friday)
            DayOfWeek.SATURDAY -> getString(R.string.day_saturday)
            DayOfWeek.SUNDAY -> getString(R.string.day_sunday)
        }
    }

    fun Context.getMonthName(month: Month): String {
        return when(month) {
            Month.JANUARY -> getString(R.string.month_january)
            Month.FEBRUARY -> getString(R.string.month_february)
            Month.MARCH -> getString(R.string.month_march)
            Month.APRIL -> getString(R.string.month_april)
            Month.MAY -> getString(R.string.month_may)
            Month.JUNE -> getString(R.string.month_june)
            Month.JULY -> getString(R.string.month_july)
            Month.AUGUST -> getString(R.string.month_august)
            Month.SEPTEMBER -> getString(R.string.month_september)
            Month.OCTOBER -> getString(R.string.month_october)
            Month.NOVEMBER -> getString(R.string.month_november)
            Month.DECEMBER -> getString(R.string.month_december)
        }
    }

    fun getFileName(context: Context, uri: Uri?): String {
        var name = context.getString(R.string.upload_popup_file_unknown)

        if (uri == null) {
            return name
        }
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    fun AchievementEntry.getDisplayText(context: Context): String {
        return when (unit) {
            AchievementUnit.HOURS ->
                context.resources.getQuantityString(
                    R.plurals.time_hours,
                    value,
                    value
                )
            AchievementUnit.DAYS ->
                context.resources.getQuantityString(
                    R.plurals.time_days,
                    value,
                    value
                )
            AchievementUnit.WEEKS ->
                context.resources.getQuantityString(
                    R.plurals.time_weeks,
                    value,
                    value
                )
            AchievementUnit.MONTHS ->
                context.resources.getQuantityString(
                    R.plurals.time_months,
                    value,
                    value
                )
            AchievementUnit.YEARS ->
                context.resources.getQuantityString(
                    R.plurals.time_years,
                    value,
                    value
                )
            AchievementUnit.CIGARETTES ->
                context.resources.getQuantityString(
                    R.plurals.cigarettes_count,
                    value,
                    value
                )
        }
    }
}