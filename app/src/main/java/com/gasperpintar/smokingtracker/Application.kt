package com.gasperpintar.smokingtracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.gasperpintar.smokingtracker.di.Container
import kotlinx.coroutines.runBlocking

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
}