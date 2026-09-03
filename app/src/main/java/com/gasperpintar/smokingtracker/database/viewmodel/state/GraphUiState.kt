package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.GraphEntry
import java.time.LocalDate

data class GraphUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val dailyEntries: List<GraphEntry> = emptyList(),
    val weeklyEntries: List<GraphEntry> = emptyList(),
    val monthlyEntries: List<GraphEntry> = emptyList(),
    val yearlyEntries: List<GraphEntry> = emptyList(),
    val dailyCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val yearlyCount: Int = 0
)