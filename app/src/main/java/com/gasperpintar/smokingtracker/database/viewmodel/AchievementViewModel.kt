package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.viewmodel.state.AchievementState
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory

class AchievementViewModel(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    suspend fun getAchievements(
        category: AchievementCategory
    ): AchievementState {
        val achievements = achievementRepository.getAll()
            .map(transform = AchievementEntry::fromEntity)
            .filter { it.category == category }
        return AchievementState(achievements = achievements)
    }
}