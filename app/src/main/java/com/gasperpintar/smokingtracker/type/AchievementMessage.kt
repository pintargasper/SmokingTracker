package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class AchievementMessage(
    val stringResource: Int
) {
    DAYS1(R.string.achievement_congrats_one_day_message),
    DAYS2(R.string.achievement_congrats_two_days_message),
    DAYS3(R.string.achievement_congrats_three_days_message),
    DAYS4(R.string.achievement_congrats_four_days_message),
    DAYS5(R.string.achievement_congrats_five_days_message),
    DAYS6(R.string.achievement_congrats_six_days_message),
    WEEK1(R.string.achievement_congrats_one_week_message),
    WEEK2(R.string.achievement_congrats_two_weeks_message),
    WEEK3(R.string.achievement_congrats_three_weeks_message),
    MONTH1(R.string.achievement_congrats_one_month_message),
    MONTH3(R.string.achievement_congrats_three_months_message),
    MONTH6(R.string.achievement_congrats_six_months_message),
    MONTH9(R.string.achievement_congrats_nine_months_message),
    YEAR1(R.string.achievement_congrats_one_year_message),
    C20(R.string.achievement_congrats_twenty_message),
    C200(R.string.achievement_congrats_two_hundred_message),
    C400(R.string.achievement_congrats_four_hundred_message),
    C600(R.string.achievement_congrats_six_hundred_message),
    C800(R.string.achievement_congrats_eight_hundred_message),
    C1000(R.string.achievement_congrats_thousand_message);
}