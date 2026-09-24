package com.gasperpintar.smokingtracker.database.fake

import com.gasperpintar.smokingtracker.database.dao.CostsDao
import com.gasperpintar.smokingtracker.database.entity.CostEntity

class FakeCostsDao : FakeDao<CostEntity>(
    idOf = { it.id },
    withId = { entity, id -> entity.copy(id = id) }
), CostsDao {

    @Override
    override suspend fun deleteAll() {
        items.clear()
    }

    @Override
    override suspend fun resetAutoIncrement() {
        resetId()
    }

    @Override
    override suspend fun getAll(): List<CostEntity> {
        return items.sortedWith(comparator = compareByDescending<CostEntity> { it.startDate }.thenByDescending { it.endDate })
    }

    @Override
    override suspend fun getLast(): CostEntity? {
        return items.maxByOrNull { it.endDate }
    }
}