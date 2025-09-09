package com.gasperpintar.smokingtracker.utils

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.model.HistoryEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.to

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
            lent = if (isLent) 1 else 0
        )
    }
}