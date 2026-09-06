package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry

data class SettingsState(
    val settings: SettingsEntity,
    val notificationsSettings: NotificationsSettingsEntity,
    val costs: List<CostEntry> = emptyList()
)