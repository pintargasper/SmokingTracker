package com.gasperpintar.smokingtracker.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gasperpintar.smokingtracker.database.viewmodel.GraphViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.HomeViewModel

class AppModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress(names = ["UNCHECKED_CAST"])
    @Override
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    historyRepository = appContainer.historyRepository,
                    achievementRepository = appContainer.achievementRepository
                ) as T
            }

            modelClass.isAssignableFrom(GraphViewModel::class.java) -> {
                GraphViewModel(historyRepository = appContainer.historyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}