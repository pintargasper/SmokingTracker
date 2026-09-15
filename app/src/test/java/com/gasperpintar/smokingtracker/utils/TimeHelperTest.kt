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
        val result = TimeHelper.getDay(date = inputDate)

        assertEquals(expectedStartOfDay, result.first)
        assertEquals(expectedEndOfDay, result.second)
    }

    @Test
    fun getWeekReturnsCorrectStartAndEndOfWeek() {
        val expectedStart = LocalDate.of(2025, 12, 29).atStartOfDay()
        val expectedEnd = LocalDate.of(2026, 1, 4).atTime(LocalTime.MAX)

        val result = TimeHelper.getWeek(inputDate)

        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun getMonthReturnsCorrectStartAndEndOfMonth() {
        val expectedStart = LocalDate.of(2025, 12, 1).atStartOfDay()
        val expectedEnd = LocalDate.of(2025, 12, 31).atTime(LocalTime.MAX)

        val result = TimeHelper.getMonth(inputDate)

        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun getYearReturnsCorrectStartAndEndOfYear() {
        val expectedStart = LocalDate.of(2025, 1, 1).atStartOfDay()
        val expectedEnd = LocalDate.of(2025, 12, 31).atTime(LocalTime.MAX)

        val result = TimeHelper.getYear(inputDate)

        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun getEndOfDayReturnsCorrectEndOfDay() {
        assertEquals(expectedEndOfDay, TimeHelper.getEndOfDay(inputDate))
    }

    @Test
    fun getNextMidnightMillisReturnsTomorrowMidnight() {
        val expected = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, TimeHelper.getNextMidnightMillis())
    }

    @Test
    fun toLocalDateTimeConvertsCalendarCorrectly() {
        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("UTC")
            set(2025, Calendar.DECEMBER, 31, 15, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expected = calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

        assertEquals(expected, TimeHelper.toLocalDateTime(calendar))
    }
}