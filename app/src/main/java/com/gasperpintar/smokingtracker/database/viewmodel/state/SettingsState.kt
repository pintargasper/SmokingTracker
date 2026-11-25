package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.model.NotificationsEntry
import com.gasperpintar.smokingtracker.database.model.SettingsEntry

data class SettingsState(
    var settings: SettingsEntry,
    var notificationsSettings: NotificationsEntry,
    val costs: List<CostEntry> = emptyList()
)