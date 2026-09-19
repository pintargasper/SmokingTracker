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
import java.time.temporal.TemporalAdjusters.previousOrSame
import java.util.Calendar

object TimeHelper {

    fun getDay(
        date: LocalDate,
        dayEndMinutes: Int = 0
    ): Pair<LocalDateTime, LocalDateTime> {
        val endTime = getDayEndTime(dayEndMinutes)
        return date.atTime(endTime) to date.plusDays(1).atTime(endTime)
    }

    fun getWeek(
        date: LocalDate,
        dayEndMinutes: Int = 0
    ): Pair<LocalDateTime, LocalDateTime> {
        val startOfWeek = date.with(previousOrSame(DayOfWeek.MONDAY))
        val endTime = getDayEndTime(dayEndMinutes)
        return startOfWeek.atTime(endTime) to startOfWeek.plusDays(7).atTime(endTime)
    }

    fun getMonth(
        date: LocalDate,
        dayEndMinutes: Int = 0
    ): Pair<LocalDateTime, LocalDateTime> {
        val endTime = getDayEndTime(dayEndMinutes)
        return date.withDayOfMonth(1).atTime(endTime) to date.withDayOfMonth(1).plusMonths(1).atTime(endTime)
    }

    fun getYear(
        date: LocalDate,
        dayEndMinutes: Int = 0
    ): Pair<LocalDateTime, LocalDateTime> {
        val endTime = getDayEndTime(dayEndMinutes)
        return date.withDayOfYear(1).atTime(endTime) to date.withDayOfYear(1).plusYears(1).atTime(endTime)
    }

    fun dayDate(dayEndMinutes: Int): LocalDate {
        return LocalDate.now().let {
            if (LocalTime.now() < getDayEndTime(dayEndMinutes)) it.minusDays(1) else it
        }
    }

    fun dayDate(
        dateTime: LocalDateTime,
        dayEndMinutes: Int
    ): LocalDate {
        return dateTime.toLocalDate().let {
            if (dateTime.toLocalTime() < getDayEndTime(dayEndMinutes)) it.minusDays(1) else it
        }
    }

    fun getEndOfDay(
        date: LocalDate
    ): LocalDateTime {
        return date.atTime(LocalTime.MAX)
    }

    fun updateDayDate(
        selectedDate: LocalDate,
        oldDayEndMinutes: Int,
        newDayEndMinutes: Int
    ): LocalDate {
        return dayDate(newDayEndMinutes).takeIf {
            selectedDate == dayDate(oldDayEndMinutes)
        } ?: selectedDate
    }

    fun getNextMidnightMillis(dayEndMinutes: Int): Long {
        val endTime = getDayEndTime(dayEndMinutes)
        return LocalDate.now().atTime(endTime)
            .plusDays(if (LocalTime.now() < endTime) 0 else 1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun formatDuration(resources: Resources, duration: Duration?): String {
        if (duration == null || duration.isZero || duration.isNegative) {
            return "0${resources.getString(R.string.home_timer_second)}"
        }

        val days = duration.toDays()
        val hours = (duration.toHours() % 24).toInt()
        val minutes = (duration.toMinutes() % 60).toInt()
        val seconds = (duration.seconds % 60).toInt()

        return buildList {
            if (days > 0) add("$days${resources.getString(R.string.home_timer_day)}")
            if (hours > 0) add("$hours${resources.getString(R.string.home_timer_hour)}")
            if (minutes > 0) add("$minutes${resources.getString(R.string.home_timer_minute)}")
            add("$seconds${resources.getString(R.string.home_timer_second)}")
        }.joinToString(separator = " ")
    }

    fun getDurationString(
        resources: Resources,
        start: LocalDateTime,
        end: LocalDateTime = LocalDateTime.now()
    ): String {
        if (start.isAfter(end)) return ""

        val period = Period.between(start.toLocalDate(), end.toLocalDate())
        val remainingStart = start.plusYears(period.years.toLong()).plusMonths(period.months.toLong())
        val duration = Duration.between(remainingStart, end)

        val years = period.years
        val months = period.months
        val days = duration.toDays().toInt()
        val hours = (duration.toHours() % 24).toInt()
        val minutes = (duration.toMinutes() % 60).toInt()

        if (years == 0 && months == 0 && days == 0 && hours == 0 && minutes == 0) {
            return resources.getQuantityString(R.plurals.time_minutes, 0, 0)
        }

        return listOfNotNull(
            years.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.time_years, it, it) },
            months.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.time_months, it, it) },
            days.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.time_days, it, it) },
            hours.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.time_hours, it, it) },
            minutes.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.time_minutes, it, it) }
        ).joinToString(separator = " ")
    }

    fun formatTime(
        resources: Resources,
        totalMinutes: Int
    ): String {
        if (totalMinutes <= 0) return resources.getQuantityString(R.plurals.time_minutes, 0, 0)

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return listOfNotNull(
            hours.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.time_hours, it, it) },
            minutes.takeIf { it > 0 || hours == 0 }?.let { resources.getQuantityString(R.plurals.time_minutes, it, it) }
        ).joinToString(separator = " ")
    }

    fun toLocalDateTime(calendar: Calendar): LocalDateTime {
        return calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    fun applySelectedDate(
        startDate: Calendar?,
        endDate: Calendar?,
        selectedDate: Calendar,
        isStartDate: Boolean
    ): Triple<Calendar?, Calendar?, String> {
        val selected = selectedDate.clone() as Calendar

        val start = if (isStartDate)
            endDate?.takeIf { selected.after(it) }?.clone() as? Calendar ?: selected
        else startDate

        val end = if (!isStartDate)
            startDate?.takeIf { selected.before(it) }?.clone() as? Calendar ?: selected
        else endDate

        val date = (if (isStartDate) start else end ?: selected)!!.time.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .let { LocalizationHelper.formatDate(it) }
        return Triple(start, end, date)
    }

    private fun getDayEndTime(dayEndMinutes: Int): LocalTime {
        return LocalTime.of(dayEndMinutes / 60, dayEndMinutes % 60)
    }
}