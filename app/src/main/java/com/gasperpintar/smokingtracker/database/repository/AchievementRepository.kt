package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.model.AchievementJson

class AchievementRepository(
    private val achievementDao: AchievementDao
) {
    suspend fun getAll(): List<AchievementEntry> {
        return achievementDao.all().map(transform = AchievementEntry::fromEntity)
    }

    suspend fun getByCategory(
        category: AchievementJson.Category
    ): List<AchievementEntry> {
        return achievementDao.byCategory(category).map(transform = AchievementEntry::fromEntity)
    }

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

    suspend fun upsert(
        entries: List<AchievementEntry>
    ) {
        achievementDao.upsertAll(entities = entries.map(transform = AchievementEntry::toEntity))
    }

    suspend fun delete(entry: AchievementEntry? = null) {
        entry?.let {
            achievementDao.delete(entity = it.toEntity())
        } ?: achievementDao.deleteAll()
    }

    suspend fun reset(
        state: Boolean
    ) {
        achievementDao.reset(state = state)
    }
}