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

    @Test
    fun dayDateBeforeDayEndBelongsToPreviousDay() {
        val dateTime = LocalDateTime.of(inputDate, LocalTime.of(1, 30))

        assertEquals(inputDate.minusDays(1), TimeHelper.dayDate(dateTime = dateTime, dayEndMinutes = 120))
    }

    @Test
    fun dayDateAtDayEndBelongsToSameDay() {
        val dateTime = LocalDateTime.of(inputDate, LocalTime.of(2, 0))

        assertEquals(inputDate, TimeHelper.dayDate(dateTime = dateTime, dayEndMinutes = 120))
        assertEquals(inputDate, TimeHelper.dayDate(dateTime = expectedStartOfDay, dayEndMinutes = 0))
    }

    @Test
    fun updateDayDateKeepsSelectedDateWhenItIsNotCurrentDay() {
        val selectedDate = LocalDate.of(2020, 1, 1)

        assertEquals(
            selectedDate,
            TimeHelper.updateDayDate(selectedDate = selectedDate, oldDayEndMinutes = 0, newDayEndMinutes = 120)
        )
    }

    @Test
    fun applySelectedDateKeepsValidRange() {
        val startDate = calendar(date = LocalDate.of(2026, 1, 1))
        val selectedDate = calendar(date = LocalDate.of(2026, 1, 10))

        val (start, end, text) = TimeHelper.applySelectedDate(
            startDate = startDate,
            endDate = null,
            selectedDate = selectedDate,
            isStartDate = false
        )

        assertEquals(startDate, start)
        assertEquals(selectedDate, end)
        assertEquals(LocalizationHelper.formatDate(LocalDate.of(2026, 1, 10)), text)
    }

    @Test
    fun applySelectedDateClampsStartDateToEndDate() {
        val endDate = calendar(date = LocalDate.of(2026, 1, 10))

        val (start, end, text) = TimeHelper.applySelectedDate(
            startDate = null,
            endDate = endDate,
            selectedDate = calendar(date = LocalDate.of(2026, 1, 15)),
            isStartDate = true
        )

        assertEquals(endDate, start)
        assertEquals(endDate, end)
        assertEquals(LocalizationHelper.formatDate(LocalDate.of(2026, 1, 10)), text)
    }

    @Test
    fun applySelectedDateClampsEndDateToStartDate() {
        val startDate = calendar(date = LocalDate.of(2026, 1, 10))

        val (start, end, text) = TimeHelper.applySelectedDate(
            startDate = startDate,
            endDate = null,
            selectedDate = calendar(date = LocalDate.of(2026, 1, 5)),
            isStartDate = false
        )

        assertEquals(startDate, start)
        assertEquals(startDate, end)
        assertEquals(LocalizationHelper.formatDate(LocalDate.of(2026, 1, 10)), text)
    }

    @Test
    fun getWeekStartsOnMonday() {
        val monday = LocalDateTime.of(LocalDate.of(2025, 12, 29), LocalTime.MIDNIGHT)

        assertEquals(monday, TimeHelper.getWeek(date = LocalDate.of(2026, 1, 4)).first)
        assertEquals(monday, TimeHelper.getWeek(date = LocalDate.of(2025, 12, 29)).first)
    }

    private fun calendar(date: LocalDate): Calendar {
        return Calendar.getInstance().apply {
            clear()
            set(date.year, date.monthValue - 1, date.dayOfMonth, 10, 0, 0)
        }
    }
}