package com.gasperpintar.smokingtracker.repository

import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

class SettingsRepository(private val settingsDao: SettingsDao) {

    suspend fun insert(settings: SettingsEntity) {
        settingsDao.insert(entity = settings)
    }

    suspend fun update(settings: SettingsEntity) {
        settingsDao.update(entity = settings)
    }

    suspend fun upsert(settings: SettingsEntity) {
        if (this.get() == null) {
            settingsDao.insert(entity = settings)
        } else {
            settingsDao.update(entity = settings)
        }
    }

    suspend fun delete(settings: SettingsEntity) {
        settingsDao.delete(entity = settings)
    }

    suspend fun get(): SettingsEntity? {
        return settingsDao.get()
    }
}
