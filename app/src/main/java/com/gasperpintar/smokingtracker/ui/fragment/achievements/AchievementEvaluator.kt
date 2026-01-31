package com.gasperpintar.smokingtracker.ui.fragment.achievements

import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import java.time.Duration
import java.time.LocalDateTime

class AchievementEvaluator(
    private val historyRepository: HistoryRepository,
    private val achievementRepository: AchievementRepository
) {

    private val cachedAchievements = mutableSetOf<AchievementEntity>()
    private var cachedAverageCigarettesPerDay: Double? = null

    suspend fun evaluate(
        lastSmokeTime: LocalDateTime,
        now: LocalDateTime
    ) {
        if (cachedAchievements.isEmpty()) {
            val achievements: List<AchievementEntity> = achievementRepository.getAll()
            achievements.forEach { achievement ->
                cachedAchievements.add(achievement)
            }
        }

        val secondsWithoutSmoking = Duration.between(lastSmokeTime, now).seconds

        val averageCigarettesPerDay: Double = cachedAverageCigarettesPerDay
            ?: historyRepository.getAverageCigarettesPerDay().also {
                cachedAverageCigarettesPerDay = it
            }

        cachedAchievements.forEach { achievement ->
            val isConditionMet = when (achievement.category) {
                AchievementCategory.SMOKE_FREE_TIME -> {
                    val requiredSeconds =
                        achievement.unit.toSeconds(achievement.value) ?: return@forEach
                    secondsWithoutSmoking >= requiredSeconds
                }

                AchievementCategory.CIGARETTES_AVOIDED -> {
                    cigarettesAvoided(lastSmokeTime, now, averageCigarettesPerDay) >= achievement.value
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
        cachedAverageCigarettesPerDay = null
    }

    private fun cigarettesAvoided(
        lastSmokeTime: LocalDateTime,
        now: LocalDateTime,
        averageCigarettesPerDay: Double
    ): Int {
        if (averageCigarettesPerDay <= 0.0) {
            return 0
        }

        val secondsWithoutSmoking: Double = Duration.between(lastSmokeTime, now).seconds.toDouble()
        val secondsPerDay = 86_400.0

        val avoidedCigarettes: Double = (secondsWithoutSmoking / secondsPerDay) * averageCigarettesPerDay
        return avoidedCigarettes.toInt()
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
        achievementRepository.incrementAchievementTimes(achievementId = achievement.id)
    }
}