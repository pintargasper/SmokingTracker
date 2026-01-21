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

    @Insert
    suspend fun insertAll(entities: List<AchievementEntity>)

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    suspend fun getAllAchievements(): List<AchievementEntity>
}
