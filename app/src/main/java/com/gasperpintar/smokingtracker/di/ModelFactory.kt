package com.gasperpintar.smokingtracker.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.MainViewModel

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
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}