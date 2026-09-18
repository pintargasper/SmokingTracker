package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.model.SettingsEntry

data class MainState(
    val settings: SettingsEntry,
    val isNewVersion: Boolean = false
)