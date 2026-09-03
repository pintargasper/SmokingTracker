package com.gasperpintar.smokingtracker.di

import android.content.Context
import com.gasperpintar.smokingtracker._interface.Container
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotesRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository

class AppContainer(context: Context): Container {

    private val database = Provider.getDatabase(context = context)

    @get:Override
    override val achievementRepository = AchievementRepository(
        achievementDao = database.achievementDao()
    )

    @get:Override
    override val costsRepository = CostsRepository(
        costDao = database.costsDao()
    )

    @get:Override
    override val historyRepository = HistoryRepository(
        historyDao = database.historyDao()
    )

    @get:Override
    override val notesRepository = NotesRepository(
        notesDao = database.notesDao()
    )

    @get:Override
    override val notificationsSettingsRepository = NotificationsSettingsRepository(
        notificationsSettingsDao = database.notificationsSettingsDao()
    )

    @get:Override
    override val settingsRepository = SettingsRepository(
        settingsDao = database.settingsDao()
    )
}