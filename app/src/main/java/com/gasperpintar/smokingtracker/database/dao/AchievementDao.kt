package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity

@Dao
interface AchievementDao {

    @Update
    suspend fun update(achievementEntity: AchievementEntity)

    @Query(value = """
        UPDATE achievements
        SET times = times + 1,
            reset = 0,
            lastCompletedAt = CURRENT_TIMESTAMP
        WHERE id = :achievementId
        AND reset = 1
    """)
    suspend fun incrementAchievementTimesSafe(
        achievementId: Long
    )

    @Query(value = """
        UPDATE achievements
        SET reset = :state
    """
    )
    suspend fun resetAllAchievements(state: Boolean)

    @Insert
    suspend fun insertAll(entities: List<AchievementEntity>)

    @Query(value = "SELECT * FROM achievements ORDER BY id ASC")
    suspend fun getAllAchievements(): List<AchievementEntity>
}
