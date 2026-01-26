package com.gasperpintar.smokingtracker.repository

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity

class AchievementRepository(
    private val achievementDao: AchievementDao
) {
    suspend fun insert(
        entry: AchievementEntity
    ) {
        achievementDao.insert(entity = entry)
    }

    suspend fun insert(
        entries: List<AchievementEntity>
    ) {
        achievementDao.insertAll(entities = entries)
    }

    suspend fun update(
        entry: AchievementEntity
    ) {
        achievementDao.update(entity = entry)
    }

    suspend fun delete(
        entry: AchievementEntity
    ) {
        achievementDao.delete(entity = entry)
    }

    suspend fun deleteAll() {
        achievementDao.deleteAll()
        achievementDao.resetAutoIncrement()
    }

    suspend fun getAll(): List<AchievementEntity> {
        return achievementDao.getAll()
    }

    suspend fun incrementAchievementTimes(
        achievementId: Long
    ) {
        achievementDao.incrementAchievementTimesSafe(achievementId = achievementId)
    }

    suspend fun resetAll(
        state: Boolean
    ) {
        achievementDao.resetAll(state = state)
    }
}
