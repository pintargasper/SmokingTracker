package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

@Dao
interface SettingsDao: Base<SettingsEntity> {

    @Query(value = "SELECT * FROM settings LIMIT 1")
    suspend fun getSettings(): SettingsEntity?
}