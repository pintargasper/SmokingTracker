package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

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