package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.TriggerEntity

@Dao
interface TriggerDao : Base<TriggerEntity> {

    @Query(value = "SELECT * FROM triggers ORDER BY id ASC")
    suspend fun getAll(): List<TriggerEntity>
}