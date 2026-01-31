package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity

@Dao
interface NotificationsSettingsDao: Base<NotificationsSettingsEntity> {

    @Query(value = "SELECT * FROM notifications_settings LIMIT 1")
    suspend fun get(): NotificationsSettingsEntity?
}