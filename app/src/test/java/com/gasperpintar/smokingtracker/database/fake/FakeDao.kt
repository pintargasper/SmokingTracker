package com.gasperpintar.smokingtracker.database.fake

import com.gasperpintar.smokingtracker.database.dao.Base

open class FakeDao<T>(
    private val idOf: (T) -> Long,
    private val withId: (T, Long) -> T
) : Base<T> {

    val items = mutableListOf<T>()
    private var nextId = 1L

    @Override
    override suspend fun upsertAll(entities: List<T>) {
        entities.forEach { upsert(entity = it) }
    }

    @Override
    override suspend fun upsert(entity: T) {
        val index = items.indexOfFirst { idOf(it) == idOf(entity) }
        if (idOf(entity) != 0L && index != -1) items[index] = entity else insert(entity = entity)
    }

    @Override
    override suspend fun insert(entity: T) {
        val id = idOf(entity).takeIf { it != 0L } ?: nextId
        nextId = maxOf(nextId, id + 1)
        items += withId(entity, id)
    }

    @Override
    override suspend fun insertAll(entities: List<T>) {
        entities.forEach { insert(entity = it) }
    }

    @Override
    override suspend fun update(entity: T) {
        val index = items.indexOfFirst { idOf(it) == idOf(entity) }
        if (index != -1) items[index] = entity
    }

    @Override
    override suspend fun delete(entity: T) {
        items.removeAll { idOf(it) == idOf(entity) }
    }

    protected fun resetId() {
        nextId = (items.maxOfOrNull(idOf) ?: 0L) + 1
    }
}
