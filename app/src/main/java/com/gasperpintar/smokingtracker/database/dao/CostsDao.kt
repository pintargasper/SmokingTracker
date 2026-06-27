package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.CostEntity

@Dao
interface CostsDao: Base<CostEntity> {

    @Query(value = "DELETE FROM costs")
    suspend fun deleteAll()

    @Query(value = "DELETE FROM sqlite_sequence WHERE name = 'costs'")
    suspend fun resetAutoIncrement()

    @Query(value = "SELECT * FROM costs ORDER BY startDate DESC, endDate DESC")
    suspend fun getAll(): List<CostEntity>

    @Query(value = "SELECT * FROM costs ORDER BY endDate DESC LIMIT 1")
    suspend fun getLast(): CostEntity?
}