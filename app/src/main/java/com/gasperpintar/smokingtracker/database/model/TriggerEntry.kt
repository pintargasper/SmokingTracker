package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.database.entity.TriggerEntity
import com.gasperpintar.smokingtracker.model.TriggerJson

data class TriggerEntry(
    @get:Override
    override val id: Long,
    val type: TriggerJson.Type,
    val value: Int = 0,
    val percent: Double = 0.0
): IIdentifiable {
    companion object {
        fun fromEntity(
            entity: TriggerEntity
        ): TriggerEntry {
            return TriggerEntry(id = entity.id, type = entity.type)
        }
    }

    fun toEntity(): TriggerEntity {
        return TriggerEntity(id = id, type = type)
    }
}