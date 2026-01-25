package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications_settings")
data class NotificationsSettingsEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val system: Boolean,
    val achievements: Boolean,
)