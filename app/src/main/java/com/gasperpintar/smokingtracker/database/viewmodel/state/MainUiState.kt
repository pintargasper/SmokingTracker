package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.database.entity.SettingsEntity

data class MainUiState(
    val settings: SettingsEntity? = null
)