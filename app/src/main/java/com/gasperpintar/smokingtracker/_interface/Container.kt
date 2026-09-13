package com.gasperpintar.smokingtracker._interface

import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotesRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository

interface Container {
    val achievementRepository: AchievementRepository
    val costsRepository: CostsRepository
    val historyRepository: HistoryRepository
    val notesRepository: NotesRepository
    val notificationsSettingsRepository: NotificationsSettingsRepository
    val settingsRepository: SettingsRepository
}