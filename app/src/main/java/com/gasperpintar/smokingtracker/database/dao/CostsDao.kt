package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gasperpintar.smokingtracker.database.entity.CostEntity

@Dao
interface CostsDao: Base<CostEntity> {

    @Query("""
        SELECT 
        EXISTS(
            SELECT 1 
            FROM costs
        )
    """)
    suspend fun exists(): Boolean

    @Query(value = """
        SELECT * 
        FROM costs 
        ORDER BY startDate DESC, endDate DESC
    """)
    suspend fun all(): List<CostEntity>

    @Query(value = """
        SELECT * 
        FROM costs 
        ORDER BY endDate DESC 
        LIMIT 1
    """)
    suspend fun last(): CostEntity?

    @Transaction
    suspend fun deleteAll() {
        delete()
        resetAutoIncrement()
    }

    @Query("""
        DELETE FROM costs
    """)
    suspend fun delete()

    @Query(value = """
        DELETE FROM sqlite_sequence 
        WHERE name = 'costs'
    """)
    suspend fun resetAutoIncrement()
}