package com.gasperpintar.smokingtracker.utils

import android.content.res.Resources
import com.gasperpintar.smokingtracker.R
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

    @Deprecated(
        message = "Since version 1.3.0. Use getEndOfDay(date: LocalDate) instead",
        replaceWith = ReplaceWith(expression = "getEndOfDay(date)")
    )
    fun getEndOfDay(): LocalDateTime {
        return LocalDateTime.now().withHour(23)
            .withMinute(59).withSecond(59)
            .withNano(999_999_999)
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
}