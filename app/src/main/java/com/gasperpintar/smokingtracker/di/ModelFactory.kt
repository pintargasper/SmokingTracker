package com.gasperpintar.smokingtracker.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.AchievementViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.CalculatorViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.GraphViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.MainViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel

class ModelFactory(
    private val application: Application? = null,
    private val container: Container
) : ViewModelProvider.Factory {

    @Suppress(names = ["UNCHECKED_CAST"])
    @Override
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    application = application!!,
                    achievementRepository = container.achievementRepository,
                    costsRepository = container.costsRepository,
                    settingsRepository = container.settingsRepository,
                    notificationsSettingsRepository = container.notificationsSettingsRepository
                ) as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    achievementRepository = container.achievementRepository,
                    historyRepository = container.historyRepository,
                ) as T
            }

            modelClass.isAssignableFrom(GraphViewModel::class.java) -> {
                GraphViewModel(
                    historyRepository = container.historyRepository,
                ) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    achievementRepository = container.achievementRepository,
                    costsRepository = container.costsRepository,
                    historyRepository = container.historyRepository,
                    notesRepository = container.notesRepository,
                    notificationsSettingsRepository = container.notificationsSettingsRepository,
                    settingsRepository = container.settingsRepository
                ) as T
            }

            modelClass.isAssignableFrom(CalculatorViewModel::class.java) -> {
                CalculatorViewModel(
                    settingsRepository = container.settingsRepository,
                ) as T
            }

            modelClass.isAssignableFrom(AchievementViewModel::class.java) -> {
                AchievementViewModel(
                    achievementRepository = container.achievementRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}