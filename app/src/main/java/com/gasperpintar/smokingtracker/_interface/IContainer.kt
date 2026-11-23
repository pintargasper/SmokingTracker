package com.gasperpintar.smokingtracker._interface

import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotesRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.repository.TriggerRepository

interface IContainer {
    val achievementRepository: AchievementRepository
    val costsRepository: CostsRepository
    val historyRepository: HistoryRepository
    val notesRepository: NotesRepository
    val notificationsRepository: NotificationsRepository
    val settingsRepository: SettingsRepository
    val triggerRepository: TriggerRepository
}