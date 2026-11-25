package com.gasperpintar.smokingtracker.database.viewmodel.state

import com.gasperpintar.smokingtracker.type.Appearance

data class MainState(
    val isNewVersion: Boolean = false,
    val startPage: Appearance.StartPage = Appearance.StartPage.HOME
)