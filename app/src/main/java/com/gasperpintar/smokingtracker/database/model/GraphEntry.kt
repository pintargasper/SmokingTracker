package com.gasperpintar.smokingtracker.database.model

import java.time.LocalDateTime

data class GraphEntry(
    val quantity: Int,
    val date: LocalDateTime
)