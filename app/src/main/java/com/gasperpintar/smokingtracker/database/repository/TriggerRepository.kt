package com.gasperpintar.smokingtracker.database.repository

import com.gasperpintar.smokingtracker.database.dao.TriggerDao
import com.gasperpintar.smokingtracker.database.model.TriggerEntry

class TriggerRepository(
    private val triggerDao: TriggerDao
) {

    suspend fun insert(entries: List<TriggerEntry>) {
        triggerDao.insertAll(entities = entries.map(transform = TriggerEntry::toEntity))
    }

    suspend fun upsertAll(entries: List<TriggerEntry>) {
        triggerDao.upsertAll(entities = entries.map(transform = TriggerEntry::toEntity))
    }

    suspend fun getAll(): List<TriggerEntry> {
        return triggerDao.getAll().map(transform = TriggerEntry::fromEntity)
    }
}