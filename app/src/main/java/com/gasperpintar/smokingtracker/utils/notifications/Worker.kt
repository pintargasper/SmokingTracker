package com.gasperpintar.smokingtracker.utils.notifications

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.dao.AchievementDao
import com.gasperpintar.smokingtracker.database.dao.HistoryDao
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.Duration
import java.time.LocalDateTime

class Worker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext = context,
    workerParams
) {

    private val database: AppDatabase by lazy { Provider.getDatabase(applicationContext) }

    @RequiresPermission(value = Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val historyDao: HistoryDao = database.historyDao()
        val achievementsDao: AchievementDao = database.achievementDao()

        val lastHistory = historyDao.getLast()
        val achievements = achievementsDao.getAll()

        val duration: Duration? = lastHistory?.let {
            Duration.between(it.createdAt, LocalDateTime.now())
        }

        if (duration != null && duration.toHours() >= 1) {
            Notifications.createNotificationChannel(applicationContext)
            Notifications.sendNotification(
                context = applicationContext,
                title = applicationContext.getString(R.string.notification_title),
                content = applicationContext.getString(
                    R.string.notification_content,
                    TimeHelper.formatDuration(resources = applicationContext.resources, duration = duration)
                ),
                notificationId = 1001
            )
        }

        val baseAchievementNotificationId = 1002
        achievements.forEachIndexed { index, achievement ->
            if (!achievement.reset && achievement.notify) {
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
                achievementsDao.update(entity = achievement.copy(notify = false))
            }
        }
        return Result.success()
    }
}