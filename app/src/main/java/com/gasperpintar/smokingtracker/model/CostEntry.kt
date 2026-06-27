package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.CostEntity
import java.time.LocalDateTime

data class CostEntry(
    override val id: Long,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val price: Double,
): Identifiable {

    companion object {

        fun fromEntity(
            entity: CostEntity
        ): CostEntry {
            return CostEntry(
                id = entity.id,
                startDate = entity.startDate,
                endDate = entity.endDate,
                price = entity.price
            )
        }
    }

    fun toEntity(): CostEntity {
        return CostEntity(
            id = id,
            startDate = startDate,
            endDate = endDate,
            price = price
        )
    }
}
