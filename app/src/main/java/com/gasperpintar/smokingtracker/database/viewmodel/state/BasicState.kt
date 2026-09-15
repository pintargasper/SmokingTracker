package com.gasperpintar.smokingtracker.database.viewmodel.state

import java.time.LocalDateTime

data class BasicState(
    val maxCigarettes: Int = 0,
    val maxCigarettesDate: String? = null,
    val minCigarettes: Int = 0,
    val minCigarettesDate: String? = null,
    val averageCigarettes: Double = 0.0,
    val totalCigarettes: Int = 0,
    val firstRecordDate: LocalDateTime? = null,
    val longestStreak: Pair<LocalDateTime, LocalDateTime>? = null,
    val hasCosts: Boolean = false,
    val totalSpent: Double = 0.0,
    val todaySpent: Double = 0.0,
    val monthSpent: Double = 0.0,
    val mostExpensiveDay: String? = null,
    val mostExpensiveDaySpent: Double = 0.0
)