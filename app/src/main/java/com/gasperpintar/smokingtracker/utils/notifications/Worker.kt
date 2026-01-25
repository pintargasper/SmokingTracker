package com.gasperpintar.smokingtracker.utils.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.Duration
import java.time.LocalDateTime

class Worker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(
    appContext = context,
    workerParams
) {

    private val database: AppDatabase by lazy { Provider.getDatabase(applicationContext) }
    private val historyRepository by lazy { HistoryRepository(historyDao = database.historyDao()) }
    private val achievementRepository by lazy { AchievementRepository(achievementDao = database.achievementDao()) }
    private val notificationsSettingsRepository by lazy { NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao()) }

    override suspend fun doWork(): Result {

        val lastHistory = historyRepository.getLast()
        val achievements = achievementRepository.getAll()
        val notifications = notificationsSettingsRepository.get()

        val duration: Duration? = lastHistory?.let {
            Duration.between(it.createdAt, LocalDateTime.now())
        }

        if (duration != null && duration.toHours() >= 1 && notifications!!.system) {
            Notifications.createNotificationChannel(applicationContext)
            Notifications.sendNotification(
                context = applicationContext,
                title = applicationContext.getString(R.string.notification_title),
                content = applicationContext.getString(
                    R.string.notification_content,
                    TimeHelper.formatDuration(
                        resources = applicationContext.resources,
                        duration = duration
                    )
                ),
                notificationId = 1001
            )
        }

        val baseAchievementNotificationId = 1002
        achievements.forEachIndexed { index, achievement ->
            if (!achievement.reset && achievement.notify) {
                if (notifications!!.achievements) {
                    Notifications.createNotificationChannel(applicationContext)
                    val notificationId = baseAchievementNotificationId + index + 1
                    Notifications.sendNotification(
                        context = applicationContext,
                        title = applicationContext.getString(R.string.notification_achievement_unlocked_title),
                        content = applicationContext.getString(
                            R.string.notification_achievement_unlocked_content,
                            AchievementEntry.fromEntity(entity = achievement).getDisplayText(applicationContext)
                        ),
                        notificationId = notificationId
                    )
                }
                achievementRepository.update(entry = achievement.copy(notify = false))
            }
        }
        return Result.success()
    }
}