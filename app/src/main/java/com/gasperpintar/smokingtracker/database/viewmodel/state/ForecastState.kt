package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.GraphEntry
import com.gasperpintar.smokingtracker.type.GraphInterval

data class ForecastState(
    val data: List<GraphEntry> = emptyList(),
    val forecast: List<GraphEntry> = emptyList(),
    val interval: GraphInterval = GraphInterval.DAILY
)