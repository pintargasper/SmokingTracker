package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.CostsDao
import com.gasperpintar.smokingtracker.database.model.CostEntry

class CostsRepository(
    private val costDao: CostsDao
) {
    suspend fun insert(
        entry: CostEntry
    ) {
        costDao.insert(entity = entry.toEntity())
    }

    suspend fun insertAll(
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
        entry: CostEntry
    ) {
        costDao.delete(entity = entry.toEntity())
    }

    suspend fun deleteAll() {
        costDao.deleteAll()
        costDao.resetAutoIncrement()
    }

    suspend fun getAll(): List<CostEntry> {
        return costDao.getAll().map(transform = CostEntry::fromEntity)
    }

    suspend fun getLast(): CostEntry? {
        return costDao.getLast()?.let(block = CostEntry::fromEntity)
    }
}