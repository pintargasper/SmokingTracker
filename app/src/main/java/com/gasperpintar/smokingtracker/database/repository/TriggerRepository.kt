package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.TriggerDao
import com.gasperpintar.smokingtracker.database.model.TriggerEntry

class TriggerRepository(
    private val triggerDao: TriggerDao
) {

    suspend fun getAll(): List<TriggerEntry> {
        return triggerDao.all().map(transform = TriggerEntry::fromEntity)
    }

    suspend fun insert(
        entries: List<TriggerEntry>
    ) {
        triggerDao.insertAll(entities = entries.map(transform = TriggerEntry::toEntity))
    }
}