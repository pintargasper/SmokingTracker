package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.viewmodel.state.AchievementState
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.model.AchievementJson

class AchievementViewModel(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    suspend fun getState(
        category: AchievementJson.Category
    ): AchievementState {
        return AchievementState(achievements = achievementRepository.getByCategory(category = category))
    }
}