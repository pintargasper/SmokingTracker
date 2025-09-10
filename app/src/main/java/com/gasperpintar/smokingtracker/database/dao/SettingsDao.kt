package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

@Dao
interface SettingsDao {

    @Insert
    suspend fun insert(settingsEntity: SettingsEntity)

    @Update
    suspend fun update(settingsEntity: SettingsEntity)

    @Query(value = "SELECT * FROM settings LIMIT 1")
    suspend fun getSettings(): SettingsEntity?
}