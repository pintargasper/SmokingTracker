package com.gasperpintar.smokingtracker.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import java.time.LocalDateTime

@Dao
interface AchievementDao {

    @Update
    suspend fun update(achievementEntity: AchievementEntity)

    @Query(value = """
        UPDATE achievements
        SET times = times + 1,
            lastCompletedAt = :now
        WHERE id = :achievementId
        AND reset = 1
    """)
    suspend fun incrementAchievementTimesSafe(
        achievementId: Long,
        now: LocalDateTime
    )

    @Query(value = """
        UPDATE achievements
        SET reset = 0
        WHERE id = :id
    """
    )
    suspend fun updateReset(id: Long)

    @Query(value = """
        UPDATE achievements
        SET reset = 1
    """
    )
    suspend fun resetAllAchievements()

    @Insert
    suspend fun insertAll(entities: List<AchievementEntity>)

    @Query(value = "SELECT * FROM achievements ORDER BY id ASC")
    suspend fun getAllAchievements(): List<AchievementEntity>
}
