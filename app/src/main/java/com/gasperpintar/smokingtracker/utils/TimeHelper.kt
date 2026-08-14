package com.gasperpintar.smokingtracker.utils

import android.content.res.Resources
import com.gasperpintar.smokingtracker.R
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.util.Calendar

object TimeHelper {

    fun getDay(
        date: LocalDate
    ): Pair<LocalDateTime, LocalDateTime> {
        return date.atStartOfDay() to date.atTime(LocalTime.MAX)
    }

    fun getWeek(
        date: LocalDate
    ): Pair<LocalDateTime, LocalDateTime> {
        return date.with(DayOfWeek.MONDAY).atStartOfDay() to
                date.with(DayOfWeek.MONDAY).plusDays(6).atTime(LocalTime.MAX)
    }

    fun getMonth(
        date: LocalDate
    ): Pair<LocalDateTime, LocalDateTime> {
        return date.withDayOfMonth(1).atStartOfDay() to
                date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX)
    }

    fun getYear(
        date: LocalDate
    ): Pair<LocalDateTime, LocalDateTime> {
        return date.withDayOfYear(1).atStartOfDay() to
                date.withDayOfYear(date.lengthOfYear()).atTime(LocalTime.MAX)
    }

    fun getEndOfDay(
        date: LocalDate
    ): LocalDateTime {
        return date.atTime(LocalTime.MAX)
    }

    fun getNextMidnightMillis(): Long {
        return LocalDate
            .now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun formatDuration(
        resources: Resources,
        duration: Duration?
    ): String {
        if (duration == null || duration.seconds <= 0) {
            return "0${resources.getString(R.string.home_timer_second)}"
        }

        val totalSeconds: Long = duration.seconds
        val days: Long = totalSeconds / 86400
        val hours: Long = (totalSeconds % 86400) / 3600
        val minutes: Long = (totalSeconds % 3600) / 60
        val seconds: Long = totalSeconds % 60

        val parts: MutableList<String> = mutableListOf()
        if (days > 0) {
            parts.add("$days${resources.getString(R.string.home_timer_day)}")
        }

        if (hours > 0) {
            parts.add("$hours${resources.getString(R.string.home_timer_hour)}")
        }

        if (minutes > 0) {
            parts.add("$minutes${resources.getString(R.string.home_timer_minute)}")
        }

        parts.add("$seconds${resources.getString(R.string.home_timer_second)}")
        return parts.joinToString(" ")
    }

    fun getDurationString(
        resources: Resources,
        start: LocalDateTime,
        end: LocalDateTime = LocalDateTime.now()
    ): String {
        if (start.isAfter(end)) {
            return ""
        }

        var current = start

        val years = Period.between(current.toLocalDate(), end.toLocalDate()).years
        current = current.plusYears(years.toLong())

        val months = Period.between(current.toLocalDate(), end.toLocalDate()).months
        current = current.plusMonths(months.toLong())

        val remainingDuration = Duration.between(current, end)

        val totalMinutes = remainingDuration.toMinutes()
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60

        return listOfNotNull(
            years.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_years, it, it)
            },
            months.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_months, it, it)
            },
            days.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_days, it.toInt(), it)
            },
            hours.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_hours, it.toInt(), it)
            },
            minutes.takeIf { it > 0 }?.let {
                resources.getQuantityString(R.plurals.time_minutes, it.toInt(), it)
            },
            (years == 0 && months == 0 && days == 0L && hours == 0L && minutes == 0L).takeIf { it }?.let {
                resources.getQuantityString(R.plurals.time_minutes, 0, 0)
            }
        ).joinToString(" ")
    }

    fun formatTime(
        resources: Resources,
        totalMinutes: Int
    ): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return buildList {
            if (hours > 0) {
                add(resources.getQuantityString(R.plurals.time_hours, hours, hours))
            }
            add(resources.getQuantityString(R.plurals.time_minutes, minutes, minutes))
        }.joinToString(" ")
    }

    fun toLocalDateTime(calendar: Calendar): LocalDateTime {
        return calendar.time.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun applySelectedDate(
        startDate: Calendar?,
        endDate: Calendar?,
        selectedDate: Calendar,
        isStartDate: Boolean
    ): Triple<Calendar?, Calendar?, String> {
        var updatedStartDate: Calendar? = startDate
        var updatedEndDate: Calendar? = endDate

        val selectedDateCopy: Calendar = selectedDate.clone() as Calendar
        if (isStartDate) {
            updatedStartDate = selectedDateCopy
            if (updatedEndDate != null && updatedEndDate.before(updatedStartDate)) {
                updatedEndDate = updatedStartDate.clone() as Calendar
            }
        } else {
            updatedEndDate = selectedDateCopy
            if (updatedStartDate != null && updatedStartDate.after(updatedEndDate)) {
                updatedEndDate = updatedStartDate.clone() as Calendar
            }
        }

        val calendarForDisplay: Calendar = when {
            isStartDate -> updatedStartDate
            else -> updatedEndDate
        } ?: selectedDateCopy

        return Triple(
            updatedStartDate,
            updatedEndDate,
            LocalizationHelper.formatDate(calendarForDisplay.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
        )
    }
}