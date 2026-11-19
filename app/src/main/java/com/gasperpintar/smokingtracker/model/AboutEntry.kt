package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker.R

data class AboutEntry(
    val title: String,
    val url: String,
    val value: String? = null,
    val contributions: List<ContributionType>? = null
) {

    @Suppress("unused")
    enum class ContributionType(
        val stringResource: Int
    ) {
        CODE(R.string.about_contributors_code),
        SLOVENIAN(R.string.about_contributors_slovenian),
        UKRAINIAN(R.string.about_contributors_ukrainian),
        SERBIAN(R.string.about_contributors_serbian),
        RUSSIAN(R.string.about_contributors_russian),
        CHINESE_SIMPLIFIED(R.string.about_contributors_chinese_simplified),
        CHINESE_TRADITIONAL(R.string.about_contributors_chinese_traditional),
        HUNGARIAN(R.string.about_contributors_hungarian)
    }
}