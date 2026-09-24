package com.gasperpintar.smokingtracker.database.fake

import com.gasperpintar.smokingtracker.database.dao.SettingsDao
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

class FakeSettingsDao : FakeDao<SettingsEntity>(
    idOf = { it.id },
    withId = { entity, id -> entity.copy(id = id) }
), SettingsDao {

    @Override
    override suspend fun get(): SettingsEntity? {
        return items.firstOrNull()
    }
}