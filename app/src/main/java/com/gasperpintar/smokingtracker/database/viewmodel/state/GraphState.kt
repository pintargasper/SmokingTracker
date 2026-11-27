package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.GraphEntry
import java.time.LocalDate
import java.time.Month

data class GraphState(
    val selectedDate: LocalDate,
    val dailyEntries: List<GraphEntry>,
    val weeklyEntries: List<GraphEntry>,
    val monthlyEntries: List<GraphEntry>,
    val yearlyEntries: List<GraphEntry>,
    val dailyCount: Int,
    val weeklyCount: Int,
    val monthlyCount: Int,
    val yearlyCount: Int,
    val dayEndMinutes: Int,
    val busiestHour: Int?,
    val weeklyAverage: Double?,
    val dailyConsumptionChange: Double?,
    val weeklyConsumptionChange: Double?,
    val monthlyConsumptionChange: Double?,
    val busiestWeekStart: LocalDate?,
    val busiestMonth: Month?,
    val bestMonth: Month?
)