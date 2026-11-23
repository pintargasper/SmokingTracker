package com.gasperpintar.smokingtracker.di

import android.content.Context
import com.gasperpintar.smokingtracker._interface.IContainer
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.NotesRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.repository.TriggerRepository

class Container(context: Context): IContainer {

    private val database = Provider.getDatabase(context = context)

    @get:Override
    override val achievementRepository = AchievementRepository(achievementDao = database.achievementDao())

    @get:Override
    override val costsRepository = CostsRepository(costDao = database.costsDao())

    @get:Override
    override val historyRepository = HistoryRepository(historyDao = database.historyDao())

    @get:Override
    override val notesRepository = NotesRepository(notesDao = database.notesDao())

    @get:Override
    override val notificationsRepository = NotificationsRepository(notificationsSettingsDao = database.notificationsSettingsDao())

    @get:Override
    override val settingsRepository = SettingsRepository(settingsDao = database.settingsDao())

    @get:Override
    override val triggerRepository = TriggerRepository(triggerDao = database.triggerDao())
}