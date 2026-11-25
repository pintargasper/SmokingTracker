package com.gasperpintar.smokingtracker.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gasperpintar.smokingtracker.model.TriggerJson

@Entity(tableName = "triggers")
data class TriggerEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: TriggerJson.Type
)