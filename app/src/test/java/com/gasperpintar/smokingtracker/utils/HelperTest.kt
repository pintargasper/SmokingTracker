package com.gasperpintar.smokingtracker.utils

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.model.HistoryEntry
import com.gasperpintar.smokingtracker.utils.Helper.toHistoryEntity
import com.gasperpintar.smokingtracker.utils.Helper.toHistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class HelperTest {

    private lateinit var inputDate: LocalDate
    private lateinit var expectedStartOfDay: LocalDateTime
    private lateinit var expectedEndOfDay: LocalDateTime

    @Before
    fun setup() {
        inputDate = LocalDate.of(2025, 12, 31)
        expectedStartOfDay = LocalDateTime.of(inputDate, LocalTime.MIDNIGHT)
        expectedEndOfDay = LocalDateTime.of(inputDate, LocalTime.MAX)
    }

    @Test
    fun getDayReturnsCorrectStartAndEndOfDay() {
        val result: Pair<LocalDateTime, LocalDateTime> = Helper.getDay(date = inputDate)
        assertEquals(expectedStartOfDay, result.first)
        assertEquals(expectedEndOfDay, result.second)
    }

    @Test
    fun getWeekReturnsCorrectStartAndEndOfWeek() {
        val expectedStartOfWeek: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 29), LocalTime.MIDNIGHT)
        val expectedEndOfWeek: LocalDateTime = LocalDateTime.of(LocalDate.of(2026, 1, 4), LocalTime.MAX)

        val result: Pair<LocalDateTime, LocalDateTime> = Helper.getWeek(date = inputDate)

        assertEquals(expectedStartOfWeek, result.first)
        assertEquals(expectedEndOfWeek, result.second)
    }

    @Test
    fun getMonthReturnsCorrectStartAndEndOfMonth() {
        val expectedStartOfMonth: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 1), LocalTime.MIDNIGHT)
        val expectedEndOfMonth: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.MAX)

        val result: Pair<LocalDateTime, LocalDateTime> = Helper.getMonth(date = inputDate)

        assertEquals(expectedStartOfMonth, result.first)
        assertEquals(expectedEndOfMonth, result.second)
    }

    @Test
    fun getYearReturnsCorrectStartAndEndOfYear() {
        val expectedStartOfYear: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 1, 1), LocalTime.MIDNIGHT)
        val expectedEndOfYear: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.MAX)

        val result: Pair<LocalDateTime, LocalDateTime> = Helper.getYear(date = inputDate)

        assertEquals(expectedStartOfYear, result.first)
        assertEquals(expectedEndOfYear, result.second)
    }

    @Test
    fun getEndOfDayReturnsCorrectEndOfDay() {
        val result: LocalDateTime = Helper.getEndOfDay(date = inputDate)
        assertEquals(expectedEndOfDay, result)
    }

    @Test
    fun historyEntityToHistoryEntryConversionIsCorrect() {
        val historyEntity = HistoryEntity(
            id = 1L,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            lent = 1
        )

        val expectedHistoryEntry = HistoryEntry(
            id = 1L,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            timerLabel = "10:30:00"
        )

        val result: HistoryEntry = historyEntity.toHistoryEntry()
        assertEquals(expectedHistoryEntry, result)
    }

    @Test
    fun historyEntryToHistoryEntityConversionIsCorrect() {
        val historyEntry = HistoryEntry(
            id = 1L,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            timerLabel = "10:30:00"
        )

        val expectedHistoryEntity = HistoryEntity(
            id = 1L,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 30, 0),
            lent = 1
        )

        val result: HistoryEntity = historyEntry.toHistoryEntity()
        assertEquals(expectedHistoryEntity, result)
    }
}