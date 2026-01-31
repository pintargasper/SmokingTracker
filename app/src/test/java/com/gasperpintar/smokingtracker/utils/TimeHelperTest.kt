package com.gasperpintar.smokingtracker.utils

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TimeHelperTest {

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
        val result: Pair<LocalDateTime, LocalDateTime> = TimeHelper.getDay(date = inputDate)
        assertEquals(expectedStartOfDay, result.first)
        assertEquals(expectedEndOfDay, result.second)
    }

    @Test
    fun getWeekReturnsCorrectStartAndEndOfWeek() {
        val expectedStartOfWeek: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 29), LocalTime.MIDNIGHT)
        val expectedEndOfWeek: LocalDateTime = LocalDateTime.of(LocalDate.of(2026, 1, 4), LocalTime.MAX)

        val result: Pair<LocalDateTime, LocalDateTime> = TimeHelper.getWeek(date = inputDate)

        assertEquals(expectedStartOfWeek, result.first)
        assertEquals(expectedEndOfWeek, result.second)
    }

    @Test
    fun getMonthReturnsCorrectStartAndEndOfMonth() {
        val expectedStartOfMonth: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 1), LocalTime.MIDNIGHT)
        val expectedEndOfMonth: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.MAX)

        val result: Pair<LocalDateTime, LocalDateTime> = TimeHelper.getMonth(date = inputDate)

        assertEquals(expectedStartOfMonth, result.first)
        assertEquals(expectedEndOfMonth, result.second)
    }

    @Test
    fun getYearReturnsCorrectStartAndEndOfYear() {
        val expectedStartOfYear: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 1, 1), LocalTime.MIDNIGHT)
        val expectedEndOfYear: LocalDateTime = LocalDateTime.of(LocalDate.of(2025, 12, 31), LocalTime.MAX)

        val result: Pair<LocalDateTime, LocalDateTime> = TimeHelper.getYear(date = inputDate)

        assertEquals(expectedStartOfYear, result.first)
        assertEquals(expectedEndOfYear, result.second)
    }

    @Test
    fun getEndOfDayReturnsCorrectEndOfDay() {
        val result: LocalDateTime = TimeHelper.getEndOfDay(date = inputDate)
        assertEquals(expectedEndOfDay, result)
    }
}