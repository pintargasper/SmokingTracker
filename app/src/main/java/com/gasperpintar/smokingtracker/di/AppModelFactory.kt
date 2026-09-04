package com.gasperpintar.smokingtracker.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gasperpintar.smokingtracker.SmokingTrackerApp
import com.gasperpintar.smokingtracker.database.viewmodel.GraphViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.MainViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.SettingsViewModel

class AppModelFactory(
    private val application: SmokingTrackerApp? = null,
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress(names = ["UNCHECKED_CAST"])
    @Override
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    application = application!!,
                    achievementRepository = appContainer.achievementRepository,
                    settingsRepository = appContainer.settingsRepository,
                    notificationsSettingsRepository = appContainer.notificationsSettingsRepository,
                    costsRepository = appContainer.costsRepository,
                ) as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    achievementRepository = appContainer.achievementRepository,
                    historyRepository = appContainer.historyRepository
                ) as T
            }

            modelClass.isAssignableFrom(GraphViewModel::class.java) -> {
                GraphViewModel(historyRepository = appContainer.historyRepository) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    achievementRepository = appContainer.achievementRepository,
                    historyRepository = appContainer.historyRepository,
                    settingsRepository = appContainer.settingsRepository,
                    notificationsSettingsRepository = appContainer.notificationsSettingsRepository,
                    costsRepository = appContainer.costsRepository,
                    notesRepository = appContainer.notesRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}