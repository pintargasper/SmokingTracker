package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.text.format.DateFormat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

object LocalizationHelper {

    fun getLocale(): Locale {
        return Locale.getDefault()
    }

    fun getLocalizedContext(context: Context, settingsRepository: SettingsRepository): Context {
        val language = runBlocking { settingsRepository.get() }?.language

        val targetLocale = language?.takeUnless { it == "system" }
            ?.let(block = Locale::forLanguageTag) ?: Resources.getSystem().configuration.locales[0]

        val config = context.resources.configuration
        if (config.locales[0] == targetLocale) return context

        Locale.setDefault(targetLocale)
        return context.createConfigurationContext(Configuration(config).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        })
    }

    fun formatDate(
        date: LocalDate,
        style: FormatStyle = FormatStyle.LONG
    ): String {
        return date.format(DateTimeFormatter.ofLocalizedDate(style).withLocale(getLocale()))
    }

    fun formatTime(
        time: LocalTime,
        style: FormatStyle = FormatStyle.SHORT
    ): String {
        return time.format(DateTimeFormatter.ofLocalizedTime(style).withLocale(getLocale()))
    }

    fun formatDateTime(
        dateTime: LocalDateTime,
        style: FormatStyle = FormatStyle.SHORT
    ): String {
        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(style).withLocale(getLocale()))
    }

    fun formatDateRange(
        start: LocalDate,
        end: LocalDate? = null,
        skeleton: String? = "ddMM"
    ): String {
        val format = DateTimeFormatter.ofPattern(
            DateFormat.getBestDateTimePattern(getLocale(), skeleton),
            getLocale()
        )

        return end?.let {
            "${start.format(format)} - ${it.format(format)}"
        } ?: start.format(format).replace(Regex("\\p{L}+")) {
            it.value.replaceFirstChar { char -> char.uppercase(getLocale()) }
        }
    }

    fun getDayOfWeekName(
        dayOfWeek: DayOfWeek,
        style: TextStyle = TextStyle.FULL
    ): String {
        return dayOfWeek.getDisplayName(style, getLocale()).replaceFirstChar { it.titlecase(locale = getLocale()) }
    }

    fun getMonthName(
        month: Month,
        style: TextStyle = TextStyle.FULL
    ): String {
        return month.getDisplayName(style, getLocale()).replaceFirstChar { it.titlecase(locale = getLocale()) }
    }

    fun formatLoggedDate(resources: Resources, day: String?): String {
        return day?.let {
            resources.getString(R.string.statistics_logged, formatDate(LocalDate.parse(it)))
        } ?: ""
    }

    suspend fun formatMoney(settingsRepository: SettingsRepository, value: Double): String {
        return "${DecimalFormat("0.00#").format(value)} ${settingsRepository.get()?.currency ?: "€"}"
    }
}