package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.NotificationsSettingsDao
import com.gasperpintar.smokingtracker.database.model.NotificationsEntry

class NotificationsRepository(
    private val notificationsSettingsDao: NotificationsSettingsDao
) {

    suspend fun get(): NotificationsEntry? {
        return notificationsSettingsDao.last()?.let(block = NotificationsEntry::fromEntity)
    }

    suspend fun insert(
        settings: NotificationsEntry
    ) {
        notificationsSettingsDao.insert(entity = settings.toEntity())
    }

    suspend fun update(
        settings: NotificationsEntry
    ) {
        notificationsSettingsDao.update(entity = settings.toEntity())
    }

    suspend fun delete(
        settings: NotificationsEntry
    ) {
        notificationsSettingsDao.delete(entity = settings.toEntity())
    }
}