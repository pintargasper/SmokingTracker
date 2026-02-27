package com.gasperpintar.smokingtracker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

object LocalizationHelper {

    fun getLocalizedContext(
        context: Context,
        settingsRepository: SettingsRepository
    ): Context {
        val settings = runBlocking { settingsRepository.get() }
        val languageId = settings?.language ?: 0
        val supportedLanguages = context.resources.getStringArray(R.array.language_values)
        val selectedLanguage = supportedLanguages.getOrNull(index = languageId) ?: "system"

        val locale: Locale = if (selectedLanguage == "system") {
            context.resources.configuration.locales.get(0)
        } else {
            Locale.forLanguageTag(selectedLanguage)
        }

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    fun formatDate(
        date: LocalDate
    ): String {
        val locale = Locale.getDefault()
        val pattern = when (locale.language) {
            "sl", "uk" -> "dd.MM.yyyy"
            else -> "yyyy-MM-dd"
        }
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        return date.format(formatter)
    }

    @SuppressLint(value = ["DefaultLocale"])
    fun formatWeekRange(
        start: LocalDate,
        end: LocalDate
    ): String {
        val locale = Locale.getDefault()
        return when (locale.language) {
            "sl", "uk" -> String.format("%02d.%02d/%02d.%02d", start.dayOfMonth, start.monthValue, end.dayOfMonth, end.monthValue)
            else -> String.format("%02d/%02d-%02d/%02d", start.monthValue, start.dayOfMonth, end.monthValue, end.dayOfMonth)
        }
    }

    fun getDayOfWeekName(
        context: Context,
        dayOfWeek: DayOfWeek
    ): String {
        return when(dayOfWeek) {
            DayOfWeek.MONDAY -> context.getString(R.string.day_monday)
            DayOfWeek.TUESDAY -> context.getString(R.string.day_tuesday)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.day_wednesday)
            DayOfWeek.THURSDAY -> context.getString(R.string.day_thursday)
            DayOfWeek.FRIDAY -> context.getString(R.string.day_friday)
            DayOfWeek.SATURDAY -> context.getString(R.string.day_saturday)
            DayOfWeek.SUNDAY -> context.getString(R.string.day_sunday)
        }
    }

    fun getMonthName(
        context: Context,
        month: Month
    ): String {
        return when(month) {
            Month.JANUARY -> context.getString(R.string.month_january)
            Month.FEBRUARY -> context.getString(R.string.month_february)
            Month.MARCH -> context.getString(R.string.month_march)
            Month.APRIL -> context.getString(R.string.month_april)
            Month.MAY -> context.getString(R.string.month_may)
            Month.JUNE -> context.getString(R.string.month_june)
            Month.JULY -> context.getString(R.string.month_july)
            Month.AUGUST -> context.getString(R.string.month_august)
            Month.SEPTEMBER -> context.getString(R.string.month_september)
            Month.OCTOBER -> context.getString(R.string.month_october)
            Month.NOVEMBER -> context.getString(R.string.month_november)
            Month.DECEMBER -> context.getString(R.string.month_december)
        }
    }
}
