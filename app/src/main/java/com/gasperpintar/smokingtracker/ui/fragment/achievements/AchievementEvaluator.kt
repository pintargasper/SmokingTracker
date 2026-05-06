package com.gasperpintar.smokingtracker.ui.fragment.achievements

import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import java.time.LocalDateTime

class AchievementEvaluator(
    private val historyRepository: HistoryRepository,
    private val achievementRepository: AchievementRepository
) {
    private val cachedAchievements = mutableMapOf<Long, AchievementEntity>()
    private var cachedAverageCigarettesPerDay: Double? = null

    suspend fun evaluate(
        lastSmokeTime: LocalDateTime, now: LocalDateTime
    ) {
        if (cachedAchievements.isEmpty()) {
            achievementRepository.getAll().forEach {
                cachedAchievements[it.id] = it
            }
        }

        val averageCigarettesPerDay = cachedAverageCigarettesPerDay
            ?: historyRepository.getAverageCigarettesPerDay().also {
                cachedAverageCigarettesPerDay = it
            }

        for (achievement in cachedAchievements.values.toList()) {
            val calculatedUnlockDate: LocalDateTime? = when (achievement.category) {
                AchievementCategory.SMOKE_FREE_TIME -> {
                    val requiredSeconds = achievement.unit.toSeconds(achievement.value) ?: continue
                    val exactTime = lastSmokeTime.plusSeconds(requiredSeconds)
                    if (!now.isBefore(exactTime)) {
                        exactTime
                    } else {
                        null
                    }
                }

                AchievementCategory.CIGARETTES_AVOIDED -> {
                    if (averageCigarettesPerDay <= 0.0) {
                        continue
                    }

                    val neededSeconds = (achievement.value / averageCigarettesPerDay * 86400).toLong()
                    val exactTime = lastSmokeTime.plusSeconds(neededSeconds)
                    if (!now.isBefore(exactTime)) {
                        exactTime
                    } else {
                        null
                    }
                }
            }

            if (calculatedUnlockDate != null) {
                val isNewAchievement = achievement.lastAchieved == null && achievement.reset
                val isIncorrectDate = achievement.lastAchieved != null && !achievement.lastAchieved.isEqual(calculatedUnlockDate)

                if (isNewAchievement || isIncorrectDate) {
                    val updatedAchievement = achievement.copy(
                        times = if (isNewAchievement) achievement.times + 1 else achievement.times,
                        lastAchieved = calculatedUnlockDate,
                        reset = false,
                        notify = isNewAchievement
                    )
                    achievementRepository.update(entry = updatedAchievement)
                    cachedAchievements[updatedAchievement.id] = updatedAchievement
                }
            }
        }
    }

    fun reset() {
        cachedAchievements.clear()
        cachedAverageCigarettesPerDay = null
    }
}