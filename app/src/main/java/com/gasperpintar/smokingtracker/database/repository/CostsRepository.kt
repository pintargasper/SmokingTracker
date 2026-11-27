package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.CostsDao
import com.gasperpintar.smokingtracker.database.model.CostEntry

class CostsRepository(
    private val costDao: CostsDao
) {
    suspend fun exists(): Boolean {
        return costDao.exists()
    }

    suspend fun getAll(): List<CostEntry> {
        return costDao.all().map(transform = CostEntry::fromEntity)
    }

    suspend fun getLast(): CostEntry? {
        return costDao.last()?.let(block = CostEntry::fromEntity)
    }

    suspend fun insert(
        entries: List<CostEntry>
    ) {
        costDao.insertAll(entities = entries.map(transform = CostEntry::toEntity))
    }

    suspend fun update(
        entry: CostEntry
    ) {
        costDao.update(entity = entry.toEntity())
    }

    suspend fun delete(
        entry: CostEntry? = null
    ) {
        entry?.let {
            costDao.delete(entity = it.toEntity())
        } ?: costDao.deleteAll()
    }
}