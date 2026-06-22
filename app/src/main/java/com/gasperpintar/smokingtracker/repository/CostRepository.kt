package com.gasperpintar.smokingtracker.repository

import com.gasperpintar.smokingtracker.database.dao.CostsDao
import com.gasperpintar.smokingtracker.database.entity.CostEntity

class CostRepository(
    private val costDao: CostsDao
) {
    suspend fun insert(
        entry: CostEntity
    ) {
        costDao.insert(entity = entry)
    }

    suspend fun insertAll(
        entries: List<CostEntity>
    ) {
        costDao.insertAll(entities = entries)
    }

    suspend fun delete(
        entry: CostEntity
    ) {
        costDao.delete(entity = entry)
    }

    suspend fun deleteAll() {
        costDao.deleteAll()
        costDao.resetAutoIncrement()
    }

    suspend fun getAll(): List<CostEntity> {
        return costDao.getAll()
    }
}
