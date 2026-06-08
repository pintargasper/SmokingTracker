package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.CostEntity

@Dao
interface CostsDao: Base<CostEntity> {

    @Query(value = "SELECT * FROM costs ORDER BY startDate DESC, endDate DESC")
    suspend fun getAll(): List<CostEntity>
}