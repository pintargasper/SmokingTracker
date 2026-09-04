package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry

data class SettingsUiState(
    val settings: SettingsEntity? = null,
    val notificationsSettings: NotificationsSettingsEntity? = null,
    val costs: List<CostEntry> = emptyList()
)