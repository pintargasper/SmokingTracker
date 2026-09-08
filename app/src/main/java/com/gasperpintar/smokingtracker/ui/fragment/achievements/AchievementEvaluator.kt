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

    suspend fun evaluate(lastSmokeTime: LocalDateTime, now: LocalDateTime) {
        val achievements = achievementRepository.getAll().ifEmpty { return }
        val averageCigarettesPerDay = historyRepository.getAverageCigarettesPerDay()
        val notifyEnabled = notificationsSettingsRepository.get()?.achievements == true
        if (notifyEnabled) Notifications.createNotificationChannel(context)

        achievements.forEach { achievement ->
            val seconds = when (achievement.category) {
                AchievementCategory.SMOKE_FREE_TIME -> achievement.unit.toSeconds(achievement.value)
                AchievementCategory.CIGARETTES_AVOIDED -> averageCigarettesPerDay.takeIf { it > 0 }?.let { (achievement.value / it * 86400).toLong() }
            } ?: return@forEach

            val unlockDate = lastSmokeTime.plusSeconds(seconds).takeIf { !now.isBefore(it) } ?: return@forEach

            val isNew = achievement.lastAchieved == null && achievement.reset
            val isOutdated = achievement.lastAchieved?.isEqual(unlockDate) == false
            if (!isNew && !isOutdated) return@forEach

            val updated = achievement.copy(
                times = achievement.times + 1,
                lastAchieved = unlockDate,
                reset = false,
                notify = !notifyEnabled
            )
            if (notifyEnabled) sendNotification(achievement = updated)
            achievementRepository.update(entry = updated)
        }
    }

    private fun sendNotification(achievement: AchievementEntity) {
        val text = AchievementEntry.fromEntity(achievement).getDisplayText(context)
        val resId = if (achievement.unit == AchievementUnit.CIGARETTES) R.string.notification_achievement_unlocked_content_cigarettes
        else R.string.notification_achievement_unlocked_content_time

        Notifications.sendNotification(
            context = context,
            title = context.getString(R.string.notification_achievement_unlocked_title),
            content = context.getString(resId, text),
            notificationId = 1002 + achievement.id.toInt()
        )
    }
}