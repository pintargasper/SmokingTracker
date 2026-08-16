package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class AchievementTitle(
    val stringResource: Int
) {
    DAYS1(R.string.achievement_congrats_one_day),
    DAYS2(R.string.achievement_congrats_two_days),
    DAYS3(R.string.achievement_congrats_three_days),
    DAYS4(R.string.achievement_congrats_four_days),
    DAYS5(R.string.achievement_congrats_five_days),
    DAYS6(R.string.achievement_congrats_six_days),
    WEEK1(R.string.achievement_congrats_one_week),
    WEEK2(R.string.achievement_congrats_two_weeks),
    WEEK3(R.string.achievement_congrats_three_weeks),
    MONTH1(R.string.achievement_congrats_one_month),
    MONTH3(R.string.achievement_congrats_three_months),
    MONTH6(R.string.achievement_congrats_six_months),
    MONTH9(R.string.achievement_congrats_nine_month),
    YEAR1(R.string.achievement_congrats_one_year),
    C20(R.string.achievement_congrats_twenty),
    C200(R.string.achievement_congrats_two_hundred),
    C400(R.string.achievement_congrats_four_hundred),
    C600(R.string.achievement_congrats_six_hundred),
    C800(R.string.achievement_congrats_eight_hundred),
    C1000(R.string.achievement_congrats_thousand);
}