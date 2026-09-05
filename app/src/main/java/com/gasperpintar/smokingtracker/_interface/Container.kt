package com.gasperpintar.smokingtracker._interface

import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository

interface Container {
    val achievementRepository: AchievementRepository
    val costsRepository: CostsRepository
    val historyRepository: HistoryRepository
    val notesRepository: NotesRepository
    val notificationsSettingsRepository: NotificationsSettingsRepository
    val settingsRepository: SettingsRepository
}