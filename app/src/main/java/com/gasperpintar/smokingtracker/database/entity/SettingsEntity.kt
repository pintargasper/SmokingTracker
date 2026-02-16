package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val theme: Int,
    val language: Int,
    val frequency: Int
)