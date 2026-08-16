package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.LocalDateTime

@RunWith(value = AndroidJUnit4::class)
class TimeHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun formatDurationReturnsZeroSecondsWhenDurationIsNull() {
        val expected = "0${context.getString(R.string.home_timer_second)}"
        val actual = TimeHelper.formatDuration(
            resources = context.resources,
            duration = null
        )

        assertEquals(expected, actual)
    }

    @Test
    fun formatDurationFormatsDaysHoursMinutesAndSeconds() {
        val duration = Duration.ofSeconds(1 * 86400L + 2 * 3600L + 3 * 60L + 4L)

        val expected = "1${context.getString(R.string.home_timer_day)} " +
                "2${context.getString(R.string.home_timer_hour)} " +
                "3${context.getString(R.string.home_timer_minute)} " +
                "4${context.getString(R.string.home_timer_second)}"

        val actual = TimeHelper.formatDuration(
            resources = context.resources,
            duration = duration
        )

        assertEquals(expected, actual)
    }

    @Test
    fun formatTimeFormatsHoursAndMinutes() {
        val expected = context.resources.getQuantityString(
            R.plurals.time_hours,
            2,
            2
        ) + " " + context.resources.getQuantityString(
            R.plurals.time_minutes,
            5,
            5
        )
        val actual = TimeHelper.formatTime(
            resources = context.resources,
            totalMinutes = 125
        )

        assertEquals(expected, actual)
    }

    @Test
    fun formatTimeFormatsOnlyMinutesWhenHoursAreZero() {
        val expected = context.resources.getQuantityString(
            R.plurals.time_minutes,
            15,
            15
            )
        val actual = TimeHelper.formatTime(
            resources = context.resources,
            totalMinutes = 15
        )

        assertEquals(expected, actual)
    }

    @Test
    fun getDurationStringFormatsYearsMonthsDaysHoursAndMinutes() {
        val start = LocalDateTime.of(2020, 1, 1, 10, 30)
        val end = LocalDateTime.of(2022, 3, 4, 13, 45)

        val expected =
            context.resources.getQuantityString(R.plurals.time_years, 2, 2) + " " +
                    context.resources.getQuantityString(R.plurals.time_months, 2, 2) + " " +
                    context.resources.getQuantityString(R.plurals.time_days, 3, 3) + " " +
                    context.resources.getQuantityString(R.plurals.time_hours, 3, 3) + " " +
                    context.resources.getQuantityString(R.plurals.time_minutes, 15, 15)

        val actual = TimeHelper.getDurationString(
            resources = context.resources,
            start = start,
            end = end
        )

        assertEquals(expected, actual)
    }
}