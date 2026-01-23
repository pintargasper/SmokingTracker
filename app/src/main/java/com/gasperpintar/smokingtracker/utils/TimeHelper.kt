package com.gasperpintar.smokingtracker.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TimeHelper {

    fun getDay(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        return date.atStartOfDay() to date.atTime(LocalTime.MAX)
    }

    fun getWeek(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        return date.with(DayOfWeek.MONDAY).atStartOfDay() to date.with(DayOfWeek.MONDAY).plusDays(6).atTime(LocalTime.MAX)
    }

    fun getMonth(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        return date.withDayOfMonth(1).atStartOfDay() to date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX)
    }

    fun getYear(date: LocalDate): Pair<LocalDateTime, LocalDateTime> {
        return date.withDayOfYear(1).atStartOfDay() to date.withDayOfYear(date.lengthOfYear()).atTime(LocalTime.MAX)
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
}