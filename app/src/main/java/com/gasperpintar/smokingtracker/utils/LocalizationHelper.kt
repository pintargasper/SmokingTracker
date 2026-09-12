package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.text.format.DateFormat
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

object LocalizationHelper {

    fun getLocalizedContext(context: Context, settingsRepository: SettingsRepository): Context {
        val language = context.resources.getStringArray(R.array.language_values)
            .getOrNull(index = runBlocking { settingsRepository.get() }?.language ?: 0)

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

    fun LocalDate.formatLocalized(): String {
        return format(DateTimeFormatter.ofLocalizedDate(
            FormatStyle.LONG).withLocale(Locale.getDefault())
        )
    }

    fun LocalDateTime.formatLocalized(): String = format(
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
    ).replace(Regex(pattern = "\\W+"), replacement = "_").trim(chars = charArrayOf('_'))

    fun formatLoggedDate(resources: Resources, day: String?): String {
        return day?.let {
            resources.getString(R.string.statistics_logged, LocalDate.parse(it).formatLocalized())
        } ?: ""
    }

    fun formatWeekRange(start: LocalDate, end: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(
            DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM")
        )
        return "${start.format(formatter)} - ${end.format(formatter)}"
    }

    suspend fun formatMoney(settingsRepository: SettingsRepository, value: Double): String {
        return "${DecimalFormat("0.00#").format(value)} ${settingsRepository.get()?.currency ?: "€"}"
    }

    fun getDayOfWeekName(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    fun getMonthName(month: Month): String {
        return month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
}