package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.content.res.Configuration
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.Provider
import kotlinx.coroutines.runBlocking
import java.util.Locale

object LocalizationHelper {

    fun getLocalizedContext(context: Context): Context {
        val database = Provider.getDatabase(context)
        val settings = runBlocking { database.settingsDao().getSettings() }
        val languageId = settings?.language ?: 0
        val supportedLanguages = context.resources.getStringArray(R.array.language_values)
        val selectedLanguage = supportedLanguages.getOrNull(languageId) ?: "system"

        val locale: Locale = if (selectedLanguage == "system") {
            context.resources.configuration.locales.get(0)
        } else Locale.forLanguageTag(selectedLanguage)

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
