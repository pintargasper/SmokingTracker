package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning

data class MinimalHomeState(
    val lastEntry: HistoryEntity? = null,
    val smokedToday: Int = 0,
    val dayEndMinutes: Int = 0,
    val elapsedTimeWarningMinutes: Int = ElapsedTimeWarning.DEFAULT_MINUTES
)
