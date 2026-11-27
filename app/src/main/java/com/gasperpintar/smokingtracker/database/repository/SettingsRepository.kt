package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.model.SettingsEntry

class SettingsRepository(
    private val settingsDao: SettingsDao
) {

    suspend fun get(): SettingsEntry? {
        return settingsDao.last()?.let(block = SettingsEntry.Companion::fromEntity)
    }

    suspend fun insert(
        settings: SettingsEntry
    ) {
        settingsDao.insert(entity = settings.toEntity())
    }

    suspend fun update(
        settings: SettingsEntry
    ) {
        settingsDao.update(entity = settings.toEntity())
    }

    suspend fun delete(
        settings: SettingsEntry
    ) {
        settingsDao.delete(entity = settings.toEntity())
    }
}