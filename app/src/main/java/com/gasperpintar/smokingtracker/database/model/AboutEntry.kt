package com.gasperpintar.smokingtracker.database.model

import com.gasperpintar.smokingtracker.type.ContributionType

data class AboutEntry(
    val title: String,
    val url: String,
    val value: String? = null,
    val contributions: List<ContributionType>? = null
)