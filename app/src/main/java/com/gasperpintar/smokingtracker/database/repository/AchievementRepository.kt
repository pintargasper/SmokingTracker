package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.model.AchievementEntry

class AchievementRepository(
    private val achievementDao: AchievementDao
) {
    suspend fun insert(
        entries: List<AchievementEntry>
    ) {
        achievementDao.insertAll(entities = entries.map(transform = AchievementEntry::toEntity))
    }

    suspend fun update(
        entry: AchievementEntry
    ) {
        achievementDao.update(entity = entry.toEntity())
    }

    suspend fun upsertAll(
        entries: List<AchievementEntry>
    ) {
        achievementDao.upsertAll(entities = entries.map(transform = AchievementEntry::toEntity))
    }

    suspend fun delete(
        entry: AchievementEntry
    ) {
        achievementDao.delete(entity = entry.toEntity())
    }

    suspend fun deleteAll() {
        achievementDao.deleteAll()
        achievementDao.resetAutoIncrement()
    }

    suspend fun getAll(): List<AchievementEntry> {
        return achievementDao.getAll().map(transform = AchievementEntry::fromEntity)
    }

    suspend fun resetAll(
        state: Boolean
    ) {
        achievementDao.resetAll(state = state)
    }
}