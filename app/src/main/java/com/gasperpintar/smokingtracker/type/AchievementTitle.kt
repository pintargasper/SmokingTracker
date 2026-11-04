package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class AchievementTitle(
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
    MONTH9(stringResource = R.string.achievement_congrats_nine_month),
    YEAR1(stringResource = R.string.achievement_congrats_one_year),
    C20(stringResource = R.string.achievement_congrats_twenty),
    C200(stringResource = R.string.achievement_congrats_two_hundred),
    C400(stringResource = R.string.achievement_congrats_four_hundred),
    C600(stringResource = R.string.achievement_congrats_six_hundred),
    C800(stringResource = R.string.achievement_congrats_eight_hundred),
    C1000(stringResource = R.string.achievement_congrats_thousand);
}