package com.gasperpintar.smokingtracker.ui.bar

class SyncedStep(
    val weight: Int,
    val action: suspend (onStepProgress: (Int) -> Unit) -> Unit
)