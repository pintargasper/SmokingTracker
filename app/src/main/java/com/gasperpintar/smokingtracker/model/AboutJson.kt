package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker.R
import com.google.gson.annotations.SerializedName

data class AboutJson(

    @SerializedName(value = "title")
    val title: String,

    @SerializedName(value = "url")
    val url: String,

    @SerializedName(value = "value")
    val value: String? = null,

    @SerializedName(value = "contributions")
    val contributions: List<ContributionType>? = null
) {

    @Suppress("unused")
    enum class ContributionType(
        val stringResource: Int
    ) {
        @SerializedName(value = "CODE")
        CODE(R.string.about_contributors_code),

        @SerializedName(value = "SLOVENIAN")
        SLOVENIAN(R.string.about_contributors_slovenian),

        @SerializedName(value = "UKRAINIAN")
        UKRAINIAN(R.string.about_contributors_ukrainian),

        @SerializedName(value = "SERBIAN")
        SERBIAN(R.string.about_contributors_serbian),

        @SerializedName(value = "RUSSIAN")
        RUSSIAN(R.string.about_contributors_russian),

        @SerializedName(value = "CHINESE_SIMPLIFIED")
        CHINESE_SIMPLIFIED(R.string.about_contributors_chinese_simplified),

        @SerializedName(value = "CHINESE_TRADITIONAL")
        CHINESE_TRADITIONAL(R.string.about_contributors_chinese_traditional),

        @SerializedName(value = "HUNGARIAN")
        HUNGARIAN(R.string.about_contributors_hungarian)
    }
}