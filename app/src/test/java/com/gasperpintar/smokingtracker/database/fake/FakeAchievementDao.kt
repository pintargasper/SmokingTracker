package com.gasperpintar.smokingtracker.database.fake

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.model.AchievementJson

class FakeAchievementDao : FakeDao<AchievementEntity>(
    idOf = { it.id },
    withId = { entity, id -> entity.copy(id = id) }
), AchievementDao {

    val resetAllCalls = mutableListOf<Boolean>()

    @Override
    override suspend fun all(): List<AchievementEntity> {
        return items.sortedBy { it.id }
    }

    @Override
    override suspend fun byCategory(category: AchievementJson.Category): List<AchievementEntity> {
        return items.filter { it.category == category }
    }

    @Override
    override suspend fun delete() {
        items.clear()
    }

    @Override
    override suspend fun resetAutoIncrement() {
        resetId()
    }

    @Override
    override suspend fun reset(state: Boolean) {
        resetAllCalls += state
        items.replaceAll { it.copy(reset = state, notify = state) }
    }
}