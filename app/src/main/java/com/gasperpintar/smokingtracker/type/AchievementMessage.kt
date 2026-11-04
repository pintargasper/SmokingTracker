package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class AchievementMessage(
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