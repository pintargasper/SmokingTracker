package com.gasperpintar.smokingtracker.repository

import com.gasperpintar.smokingtracker.database.dao.NotificationsSettingsDao
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity

class NotificationsSettingsRepository(
    private val notificationsSettingsDao: NotificationsSettingsDao
) {
    suspend fun insert(
        settings: NotificationsSettingsEntity
    ) {
        notificationsSettingsDao.insert(entity = settings)
    }

    suspend fun update(
        settings: NotificationsSettingsEntity
    ) {
        notificationsSettingsDao.update(entity = settings)
    }

    suspend fun delete(
        settings: NotificationsSettingsEntity
    ) {
        notificationsSettingsDao.delete(entity = settings)
    }

    suspend fun get(): NotificationsSettingsEntity? {
        return notificationsSettingsDao.get()
    }
}