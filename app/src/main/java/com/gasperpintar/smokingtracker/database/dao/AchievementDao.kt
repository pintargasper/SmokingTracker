package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity

@Dao
interface AchievementDao: Base<AchievementEntity> {

    @Upsert
    suspend fun upsertAll(entities: List<AchievementEntity>)

    @Query(value = "DELETE FROM achievements")
    suspend fun deleteAll()

    @Query(value = "DELETE FROM sqlite_sequence WHERE name = 'achievements'")
    suspend fun resetAutoIncrement()

    @Query(value = """
        UPDATE achievements
        SET times = times + 1,
            reset = 0,
            lastAchieved = CURRENT_TIMESTAMP
        WHERE id = :achievementId
        AND reset = 1
    """
    )
    suspend fun incrementAchievementTimesSafe(
        achievementId: Long
    )

    @Query(value = """
        UPDATE achievements
        SET 
            reset = :state,
            notify = :state
    """
    )
    suspend fun resetAll(state: Boolean)

    @Query(value = "SELECT * FROM achievements ORDER BY id ASC")
    suspend fun getAll(): List<AchievementEntity>
}
