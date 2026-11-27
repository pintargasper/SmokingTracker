package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.NotificationsEntity

@Dao
interface NotificationsSettingsDao: Base<NotificationsEntity> {

    @Query(value = """
        SELECT * 
        FROM notifications_settings 
        LIMIT 1
    """)
    suspend fun last(): NotificationsEntity?
}