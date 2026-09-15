package com.gasperpintar.smokingtracker.database.viewmodel.state

data class CalculatorState(
    val totalCost: Double = 0.0,
    val totalTimeMinutes: Int = 0,
    val totalCigarettes: Int = 0,
    val currency: String
)