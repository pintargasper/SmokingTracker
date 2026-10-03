package com.gasperpintar.smokingtracker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.gasperpintar.smokingtracker.di.Container
import kotlinx.coroutines.runBlocking
import java.util.Locale

class Application : Application() {

    val container: Container by lazy {
        val container = Container(context = this)
        runBlocking {
            applyTheme(themeId = container.settingsRepository.get()?.theme ?: 0)
        }
        container
    }

    fun applyTheme(themeId: Int) {
        when (themeId) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> Unit
        }
    }

    fun applyLanguage(context: Context): String {
        val systemLocale = Locale.getDefault()
        return when (systemLocale.language) {
            "sr" -> when (systemLocale.script) {
                "Latn" -> "sr-Latn"
                else -> "sr"
            }
            "zh" -> when (systemLocale.script) {
                "Hant" -> "zh-Hant"
                else -> "zh-Hans"
            }
            else -> context.resources.getStringArray(R.array.language_values).firstOrNull {
                if (it == "system") return@firstOrNull false
                val locale = Locale.forLanguageTag(it)
                locale.language == systemLocale.language && (locale.script.isEmpty() || locale.script == systemLocale.script)
            } ?: "system"
        }
    }
}