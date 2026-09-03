package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import java.time.LocalDate

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val history: List<HistoryEntry> = emptyList(),
    val lastEntry: HistoryEntity? = null,
    val dailyCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0
)