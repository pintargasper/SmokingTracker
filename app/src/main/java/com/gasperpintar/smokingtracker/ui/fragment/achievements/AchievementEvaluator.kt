package com.gasperpintar.smokingtracker.ui.fragment.achievements

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.type.AchievementCategory
import java.time.Duration
import java.time.LocalDateTime

class AchievementEvaluator(
    private val achievementDao: AchievementDao
) {

    private val cachedAchievements = mutableSetOf<AchievementEntity>()

    suspend fun evaluate(
        lastSmokeTime: LocalDateTime,
        now: LocalDateTime
    ) {

        if (cachedAchievements.isEmpty()) {
            val achievements: List<AchievementEntity> = achievementDao.getAllAchievements()
            achievements.forEach { achievement ->
                cachedAchievements.add(achievement)
            }
            return
        }

        val secondsWithoutSmoking = Duration.between(lastSmokeTime, now).seconds

        cachedAchievements.forEach { achievement ->
            val isConditionMet = when (achievement.category) {
                AchievementCategory.SMOKE_FREE_TIME -> {
                    val requiredSeconds =
                        achievement.unit.toSeconds(achievement.value) ?: return@forEach
                    secondsWithoutSmoking >= requiredSeconds
                }

                AchievementCategory.CIGARETTES_AVOIDED -> {
                    cigarettesAvoided(lastSmokeTime, now) >= achievement.value
                }
            }

            incrementAchievement(
                achievement = achievement,
                isConditionMet = isConditionMet
            )
        }
    }

    fun reset() {
        cachedAchievements.clear()
    }

    private fun cigarettesAvoided(lastSmokeTime: LocalDateTime, now: LocalDateTime): Int {
        val intervalSeconds = 5400L
        val sleepSecondsPerDay = 8 * 3600L
        val duration = Duration.between(lastSmokeTime, now)

        var totalSeconds = duration.seconds
        if (totalSeconds <= 0) {
            return 0
        }

        val days = totalSeconds / 86400L
        totalSeconds -= days * sleepSecondsPerDay

        val remainingSeconds = totalSeconds % 86400L
        if (remainingSeconds > 16 * 3600L) {
            totalSeconds -= sleepSecondsPerDay
        }
        return (totalSeconds / intervalSeconds).toInt()
    }

    private suspend fun incrementAchievement(
        achievement: AchievementEntity,
        isConditionMet: Boolean
    ) {
        if (!isConditionMet) {
            return
        }

        if (!achievement.reset) {
            return
        }

        achievementDao.incrementAchievementTimesSafe(
            achievementId = achievement.id
        )
    }
}