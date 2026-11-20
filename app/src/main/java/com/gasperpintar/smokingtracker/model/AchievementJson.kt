package com.gasperpintar.smokingtracker.model

import com.google.gson.annotations.SerializedName

data class AchievementJson(

    @SerializedName(value = "icon")
    val icon: String,

    @SerializedName(value = "value")
    val value: Int,

    @SerializedName(value = "title")
    val title: String,

    @SerializedName(value = "message")
    val message: String,

    @SerializedName(value = "unit")
    val unit: String
)