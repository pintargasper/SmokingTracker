package com.gasperpintar.smokingtracker.ui.fragment.achievements

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.gasperpintar.smokingtracker.utils.notifications.Notifications
import java.time.LocalDateTime

class AchievementEvaluator(
    private val context: Context,
    private val historyRepository: HistoryRepository,
    private val achievementRepository: AchievementRepository,
    private val notificationsSettingsRepository: NotificationsSettingsRepository
) {

    suspend fun evaluate(
        lastSmokeTime: LocalDateTime,
        now: LocalDateTime
    ) {

        val achievements: List<AchievementEntity> = achievementRepository.getAll()
        val averageCigarettesPerDay = historyRepository.getAverageCigarettesPerDay()

        for (achievement in achievements) {
            val calculatedUnlockDate: LocalDateTime? =
                when (achievement.category) {
                    AchievementCategory.SMOKE_FREE_TIME -> {
                        val requiredSeconds = achievement.unit.toSeconds(achievement.value) ?: continue
                        lastSmokeTime.plusSeconds(requiredSeconds).takeIf {
                            !now.isBefore(it)
                        }
                    }

                    AchievementCategory.CIGARETTES_AVOIDED -> {
                        if (averageCigarettesPerDay <= 0.0) {
                            continue
                        }

                        lastSmokeTime.plusSeconds((achievement.value / averageCigarettesPerDay * 86400).toLong()).takeIf {
                            !now.isBefore(it)
                        }
                    }
                }

            if (calculatedUnlockDate == null) {
                continue
            }

            val isNewAchievement = achievement.lastAchieved == null && achievement.reset
            val isIncorrectDate = achievement.lastAchieved != null && !achievement.lastAchieved.isEqual(calculatedUnlockDate)

            if (!isNewAchievement && !isIncorrectDate) {
                continue
            }

            val updatedAchievement = achievement.copy(
                times = achievement.times + 1,
                lastAchieved = calculatedUnlockDate,
                reset = false,
                notify = true
            )

            achievementRepository.update(entry = updatedAchievement)

            val notifications = notificationsSettingsRepository.get()

            if (notifications?.achievements == true) {
                Notifications.createNotificationChannel(context)

                val displayText = AchievementEntry.fromEntity(entity = updatedAchievement).getDisplayText(context)

                val notificationContent =
                    when (updatedAchievement.unit) {
                        AchievementUnit.CIGARETTES ->
                            context.getString(
                                R.string.notification_achievement_unlocked_content_cigarettes,
                                displayText
                            )
                        else ->
                            context.getString(
                                R.string.notification_achievement_unlocked_content_time,
                                displayText
                            )
                    }

                Notifications.sendNotification(
                    context = context,
                    title = context.getString(R.string.notification_achievement_unlocked_title),
                    content = notificationContent,
                    notificationId = 1002 + updatedAchievement.id.toInt()
                )
                achievementRepository.update(entry = updatedAchievement.copy(notify = false))
            }
        }
    }
}