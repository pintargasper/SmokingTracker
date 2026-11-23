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
        val achievements = achievementRepository.getAll()
            .filter { it.category == category }
        return AchievementState(achievements = achievements)
    }
}