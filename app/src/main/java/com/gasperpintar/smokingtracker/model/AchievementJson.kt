package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker.R
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
) {

    enum class Category {
        SMOKE_FREE_TIME,
        CIGARETTES_AVOIDED;
    }

    enum class Unit {
        YEARS,
        MONTHS,
        WEEKS,
        HOURS,
        DAYS,
        CIGARETTES;

        fun toSeconds(
            value: Int
        ): Long? = when (this) {
            HOURS -> value * 3600L
            DAYS -> value * 86400L
            WEEKS -> value * 604800L
            MONTHS -> value * 2592000L
            YEARS -> value * 31536000L
            CIGARETTES -> null
        }
    }

    enum class Icon(
        val drawableResource: Int
    ) {
        W1D(drawableResource = R.drawable.ic_1d),
        W2D(drawableResource = R.drawable.ic_2d),
        W3D(drawableResource = R.drawable.ic_3d),
        W4D(drawableResource = R.drawable.ic_4d),
        W5D(drawableResource = R.drawable.ic_5d),
        W6D(drawableResource = R.drawable.ic_6d),
        W1W(drawableResource = R.drawable.ic_1w),
        W2W(drawableResource = R.drawable.ic_2w),
        W3W(drawableResource = R.drawable.ic_3w),
        W1M(drawableResource = R.drawable.ic_1m),
        W3M(drawableResource = R.drawable.ic_3m),
        W6M(drawableResource = R.drawable.ic_6m),
        W9M(drawableResource = R.drawable.ic_9m),
        W1Y(drawableResource = R.drawable.ic_1y),
        C20(drawableResource = R.drawable.ic_20c),
        C200(drawableResource = R.drawable.ic_200c),
        C400(drawableResource = R.drawable.ic_400c),
        C600(drawableResource = R.drawable.ic_600c),
        C800(drawableResource = R.drawable.ic_800c),
        C1000(drawableResource = R.drawable.ic_1000c);
    }

    enum class Title(
        val stringResource: Int
    ) {
        DAYS1(stringResource = R.string.achievement_congrats_one_day),
        DAYS2(stringResource = R.string.achievement_congrats_two_days),
        DAYS3(stringResource = R.string.achievement_congrats_three_days),
        DAYS4(stringResource = R.string.achievement_congrats_four_days),
        DAYS5(stringResource = R.string.achievement_congrats_five_days),
        DAYS6(stringResource = R.string.achievement_congrats_six_days),
        WEEK1(stringResource = R.string.achievement_congrats_one_week),
        WEEK2(stringResource = R.string.achievement_congrats_two_weeks),
        WEEK3(stringResource = R.string.achievement_congrats_three_weeks),
        MONTH1(stringResource = R.string.achievement_congrats_one_month),
        MONTH3(stringResource = R.string.achievement_congrats_three_months),
        MONTH6(stringResource = R.string.achievement_congrats_six_months),
        MONTH9(stringResource = R.string.achievement_congrats_nine_months),
        YEAR1(stringResource = R.string.achievement_congrats_one_year),
        C20(stringResource = R.string.achievement_congrats_twenty),
        C200(stringResource = R.string.achievement_congrats_two_hundred),
        C400(stringResource = R.string.achievement_congrats_four_hundred),
        C600(stringResource = R.string.achievement_congrats_six_hundred),
        C800(stringResource = R.string.achievement_congrats_eight_hundred),
        C1000(stringResource = R.string.achievement_congrats_thousand);
    }

    enum class Message(
        val stringResource: Int
    ) {
        DAYS1(stringResource = R.string.achievement_congrats_one_day_message),
        DAYS2(stringResource = R.string.achievement_congrats_two_days_message),
        DAYS3(stringResource = R.string.achievement_congrats_three_days_message),
        DAYS4(stringResource = R.string.achievement_congrats_four_days_message),
        DAYS5(stringResource = R.string.achievement_congrats_five_days_message),
        DAYS6(stringResource = R.string.achievement_congrats_six_days_message),
        WEEK1(stringResource = R.string.achievement_congrats_one_week_message),
        WEEK2(stringResource = R.string.achievement_congrats_two_weeks_message),
        WEEK3(stringResource = R.string.achievement_congrats_three_weeks_message),
        MONTH1(stringResource = R.string.achievement_congrats_one_month_message),
        MONTH3(stringResource = R.string.achievement_congrats_three_months_message),
        MONTH6(stringResource = R.string.achievement_congrats_six_months_message),
        MONTH9(stringResource = R.string.achievement_congrats_nine_months_message),
        YEAR1(stringResource = R.string.achievement_congrats_one_year_message),
        C20(stringResource = R.string.achievement_congrats_twenty_message),
        C200(stringResource = R.string.achievement_congrats_two_hundred_message),
        C400(stringResource = R.string.achievement_congrats_four_hundred_message),
        C600(stringResource = R.string.achievement_congrats_six_hundred_message),
        C800(stringResource = R.string.achievement_congrats_eight_hundred_message),
        C1000(stringResource = R.string.achievement_congrats_thousand_message);
    }
}