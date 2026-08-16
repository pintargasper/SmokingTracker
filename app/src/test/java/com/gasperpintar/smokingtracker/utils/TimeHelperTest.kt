package com.gasperpintar.smokingtracker.utils

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

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

    @Test
    fun getNextMidnightMillisReturnsTomorrowMidnight() {
        val expected = LocalDate
            .now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val result = TimeHelper.getNextMidnightMillis()

        assertEquals(expected, result)
    }

    @Test
    fun toLocalDateTimeConvertsCalendarCorrectly() {
        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("UTC")
            set(2025, Calendar.DECEMBER, 31, 15, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expected = calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val result = TimeHelper.toLocalDateTime(calendar)

        assertEquals(expected, result)
    }
}