package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning

data class MinimalHomeState(
    val lastEntry: HistoryEntry? = null,
    val smokedToday: Int = 0,
    val dayEndMinutes: Int = 0,
    val elapsedTimeWarningMinutes: Int = ElapsedTimeWarning.DEFAULT_MINUTES
)