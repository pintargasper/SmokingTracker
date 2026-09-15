package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.AchievementEntry

data class AchievementState(
    val achievements: List<AchievementEntry> = emptyList()
)