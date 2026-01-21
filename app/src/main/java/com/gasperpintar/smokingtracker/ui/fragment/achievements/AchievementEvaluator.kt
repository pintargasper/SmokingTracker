package com.gasperpintar.smokingtracker.ui.fragment.achievements

import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.type.AchievementCategory
import java.time.Duration
import java.time.LocalDateTime

class AchievementEvaluator(
    private val achievementDao: AchievementDao
) {

    private var cachedLastSmokeTime: LocalDateTime? = null
    private val evaluatedAchievements: MutableSet<Long> = mutableSetOf()

    suspend fun evaluate(
        lastSmokeTime: LocalDateTime,
        now: LocalDateTime
    ) {
        if (cachedLastSmokeTime != lastSmokeTime) {
            cachedLastSmokeTime = lastSmokeTime
            evaluatedAchievements.clear()
        }

        val secondsWithoutSmoking: Long = Duration.between(lastSmokeTime, now).seconds
        val achievements: List<AchievementEntity> = achievementDao.getAllAchievements()

        achievements.filter { it.category == AchievementCategory.SMOKE_FREE_TIME }.forEach { achievement ->
            val requiredSeconds = achievement.unit.toSeconds(achievement.value) ?: return@forEach
            val isConditionMet = secondsWithoutSmoking >= requiredSeconds
            incrementAchievement(achievement, isConditionMet, now)
        }

        achievements.filter { it.category == AchievementCategory.CIGARETTES_AVOIDED }.forEach { achievement ->
            val intervalSeconds = 5400L
            val sleepSecondsPerDay = 8 * 3600L
            val duration = Duration.between(lastSmokeTime, now)
            var totalSeconds = duration.seconds

            if (totalSeconds <= 0) {
                return@forEach
            }

            val days = totalSeconds / 86400L
            totalSeconds -= days * sleepSecondsPerDay
            val remainingSeconds = totalSeconds % 86400L

            if (remainingSeconds > 16 * 3600L) {
                totalSeconds -= sleepSecondsPerDay
            }
            val cigarettesPassed = (totalSeconds / intervalSeconds).toInt()
            val requiredCigs = achievement.value
            val isConditionMet = cigarettesPassed >= requiredCigs
            incrementAchievement(achievement, isConditionMet, now)
        }
    }

    fun reset() {
        cachedLastSmokeTime = null
        evaluatedAchievements.clear()
    }

    private suspend fun incrementAchievement(
        achievement: AchievementEntity,
        isConditionMet: Boolean,
        now: LocalDateTime
    ) {
        if (!isConditionMet) {
            return
        }

        if (evaluatedAchievements.contains(achievement.id)) {
            return
        }

        if (!achievement.reset) {
            return
        }

        achievementDao.incrementAchievementTimesSafe(
            achievementId = achievement.id,
            now = now,
        )
        achievementDao.updateReset(achievement.id)
        evaluatedAchievements.add(achievement.id)
    }
}
