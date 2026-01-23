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
import com.gasperpintar.smokingtracker.utils.Helper.getDisplayText
import com.gasperpintar.smokingtracker.utils.Helper.toAchievementEntry
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

class Worker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext = context, workerParams) {

    private val database: AppDatabase by lazy { Provider.getDatabase(applicationContext) }

    @RequiresPermission(value = Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val historyDao: HistoryDao = database.historyDao()
        val achievementsDao: AchievementDao = database.achievementDao()

        val lastHistory = historyDao.getLastHistoryEntry()
        val achievements = achievementsDao.getAllAchievements()

        val totalSeconds = lastHistory?.let { Duration.between(it.createdAt, LocalDateTime.now()).seconds } ?: 0

        if (totalSeconds >= 3600) {
            Notifications.createNotificationChannel(applicationContext)
            Notifications.sendNotification(
                context = applicationContext,
                title = applicationContext.getString(R.string.notification_title),
                content = applicationContext.getString(
                    R.string.notification_content,
                    formatDuration(totalSeconds)
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
                        achievement.toAchievementEntry().getDisplayText(applicationContext)
                    ),
                    notificationId = notificationId
                )
                achievementsDao.update(achievementEntity = achievement.copy(notify = false))
            }
        }
        return Result.success()
    }

    private fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%dm %ds", minutes, seconds)
        }
    }
}