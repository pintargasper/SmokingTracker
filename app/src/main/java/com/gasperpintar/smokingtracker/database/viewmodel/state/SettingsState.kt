package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.model.CostEntry

data class SettingsState(
    var settings: SettingsEntity,
    var notificationsSettings: NotificationsSettingsEntity,
    val costs: List<CostEntry> = emptyList()
)