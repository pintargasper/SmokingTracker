package com.gasperpintar.smokingtracker.database.fake

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity

class FakeAchievementDao : FakeDao<AchievementEntity>(
    idOf = { it.id },
    withId = { entity, id -> entity.copy(id = id) }
), AchievementDao {

    val resetAllCalls = mutableListOf<Boolean>()

    @Override
    override suspend fun deleteAll() {
        items.clear()
    }

    @Override
    override suspend fun resetAutoIncrement() {
        resetId()
    }

    @Override
    override suspend fun resetAll(state: Boolean) {
        resetAllCalls += state
        items.replaceAll { it.copy(reset = state, notify = state) }
    }

    @Override
    override suspend fun getAll(): List<AchievementEntity> {
        return items.sortedBy { it.id }
    }
}
