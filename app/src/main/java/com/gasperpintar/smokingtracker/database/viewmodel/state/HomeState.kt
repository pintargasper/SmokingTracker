package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.model.TriggerJson
import java.time.LocalDate

data class HomeState(
    val selectedDate: LocalDate = LocalDate.now(),
    val history: List<HistoryEntry> = emptyList(),
    val lastEntry: HistoryEntry? = null,
    val dailyCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val defaultTrigger: TriggerJson.Type,
    val showIntervalLabel: Boolean
)