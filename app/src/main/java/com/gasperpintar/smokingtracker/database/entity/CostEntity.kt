package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "costs",
    indices = [
        Index(value = ["endDate"])
    ]
)
data class CostEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val price: Double,
)