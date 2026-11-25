package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.TriggerEntry

data class TriggerState(
    val mostCommon: TriggerEntry? = null,
    val triggers: List<TriggerEntry> = emptyList()
)