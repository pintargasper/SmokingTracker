package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class AchievementMessage(
    val stringResource: Int
) {
    HOURS20(R.string.achievement_message_congrats_hours_20),
    HOURS30(R.string.achievement_message_congrats_hours_30),
    HOURS60(R.string.achievement_message_congrats_hours_60),
    HOURS150(R.string.achievement_message_congrats_hours_150),
    DAYS2(R.string.achievement_message_congrats_days_2),
    DAYS5(R.string.achievement_message_congrats_days_5),
    WEEK1(R.string.achievement_message_congrats_weeks_1),
    C50(R.string.achievement_message_congrats_cigarettes_50),
    C100(R.string.achievement_message_congrats_cigarettes_100),
    C150(R.string.achievement_message_congrats_cigarettes_150),
    C200(R.string.achievement_message_congrats_cigarettes_200),
    C250(R.string.achievement_message_congrats_cigarettes_250),
    C300(R.string.achievement_message_congrats_cigarettes_300),
}