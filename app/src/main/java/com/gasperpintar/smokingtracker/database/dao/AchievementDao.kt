package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.model.AchievementJson

@Dao
interface AchievementDao: Base<AchievementEntity> {

    @Query(value = """
        SELECT * 
        FROM achievements 
        ORDER BY id ASC
    """)
    suspend fun all(): List<AchievementEntity>

    @Query("""
        SELECT *
        FROM achievements
        WHERE category = :category
        ORDER BY id ASC
    """)
    suspend fun byCategory(
        category: AchievementJson.Category
    ): List<AchievementEntity>

    @Query(value = """
        UPDATE achievements
        SET 
            reset = :state,
            notify = :state
    """)
    suspend fun reset(state: Boolean)

    @Transaction
    suspend fun deleteAll() {
        delete()
        resetAutoIncrement()
    }

    @Query("""
        DELETE FROM achievements
    """)
    suspend fun delete()

    @Query("""
        DELETE FROM sqlite_sequence
        WHERE name = 'achievements'
    """)
    suspend fun resetAutoIncrement()
}