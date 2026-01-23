package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity

@Dao
interface AchievementDao: Base<AchievementEntity> {

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
    suspend fun resetAllAchievements(state: Boolean)

    @Query(value = "SELECT * FROM achievements ORDER BY id ASC")
    suspend fun getAllAchievements(): List<AchievementEntity>
}
