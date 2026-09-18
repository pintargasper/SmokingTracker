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
        expectedEndOfDay = LocalDateTime.of(inputDate.plusDays(1), LocalTime.MIDNIGHT)
    }

    @Test
    fun getDayReturnsCorrectStartAndEndOfDay() {
        val result = TimeHelper.getDay(date = inputDate)

        assertEquals(expectedStartOfDay, result.first)
        assertEquals(expectedEndOfDay, result.second)
    }

    @Test
    fun getDayWithCustomDayEndReturnsCorrectStartAndEnd() {
        val result = TimeHelper.getDay(date = inputDate, dayEndMinutes = 120)

        assertEquals(
            LocalDateTime.of(inputDate, LocalTime.of(2, 0)),
            result.first
        )

        assertEquals(
            LocalDateTime.of(inputDate.plusDays(1), LocalTime.of(2, 0)),
            result.second
        )
    }

    @Test
    fun getWeekReturnsCorrectStartAndEndOfWeek() {
        val expectedStart = LocalDateTime.of(
            LocalDate.of(2025, 12, 29),
            LocalTime.MIDNIGHT
        )

        val expectedEnd = LocalDateTime.of(
            LocalDate.of(2026, 1, 5),
            LocalTime.MIDNIGHT
        )

        val result = TimeHelper.getWeek(inputDate)

        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun getWeekWithCustomDayEndReturnsCorrectStartAndEnd() {
        val result = TimeHelper.getWeek(date = inputDate, dayEndMinutes = 120)

        assertEquals(
            LocalDateTime.of(
                LocalDate.of(2025, 12, 29),
                LocalTime.of(2, 0)
            ), result.first
        )

        assertEquals(
            LocalDateTime.of(
                LocalDate.of(2026, 1, 5),
                LocalTime.of(2, 0)
            ), result.second
        )
    }

    @Test
    fun getMonthReturnsCorrectStartAndEndOfMonth() {
        val expectedStart = LocalDateTime.of(
            LocalDate.of(2025, 12, 1),
            LocalTime.MIDNIGHT
        )

        val expectedEnd = LocalDateTime.of(
            LocalDate.of(2026, 1, 1),
            LocalTime.MIDNIGHT
        )

        val result = TimeHelper.getMonth(inputDate)

        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun getMonthWithCustomDayEndReturnsCorrectStartAndEnd() {
        val result = TimeHelper.getMonth(date = inputDate, dayEndMinutes = 120)
        assertEquals(
            LocalDateTime.of(
                LocalDate.of(2025, 12, 1),
                LocalTime.of(2, 0)
            ), result.first
        )

        assertEquals(
            LocalDateTime.of(
                LocalDate.of(2026, 1, 1),
                LocalTime.of(2, 0)
            ), result.second
        )
    }

    @Test
    fun getYearReturnsCorrectStartAndEndOfYear() {
        val expectedStart = LocalDateTime.of(LocalDate.of(2025, 1, 1), LocalTime.MIDNIGHT)
        val expectedEnd = LocalDateTime.of(LocalDate.of(2026, 1, 1), LocalTime.MIDNIGHT)
        val result = TimeHelper.getYear(inputDate)

        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun getYearWithCustomDayEndReturnsCorrectStartAndEnd() {
        val result = TimeHelper.getYear(date = inputDate, dayEndMinutes = 120)

        assertEquals(
            LocalDateTime.of(
                LocalDate.of(2025, 1, 1),
                LocalTime.of(2, 0)),
            result.first
        )

        assertEquals(LocalDateTime.of(
            LocalDate.of(2026, 1, 1),
            LocalTime.of(2, 0)),
            result.second
        )
    }

    @Test
    fun getEndOfDayReturnsCorrectEndOfDay() {
        assertEquals(LocalDateTime.of(inputDate, LocalTime.MAX), TimeHelper.getEndOfDay(inputDate))
    }

    @Test
    fun getNextMidnightMillisReturnsTomorrowMidnight() {
        val expected = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, TimeHelper.getNextMidnightMillis(dayEndMinutes = 0))
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