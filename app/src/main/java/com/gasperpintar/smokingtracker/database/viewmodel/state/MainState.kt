package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.type.StartPage

data class MainState(
    val isNewVersion: Boolean = false,
    val startPage: StartPage = StartPage.HOME
)