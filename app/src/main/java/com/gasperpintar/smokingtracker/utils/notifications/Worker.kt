package com.gasperpintar.smokingtracker.utils.notifications

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementEvaluator
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

    private val database: AppDatabase by lazy {
        Provider.getDatabase(applicationContext)
    }

    private val historyRepository: HistoryRepository by lazy {
        HistoryRepository(historyDao = database.historyDao())
    }

    private val achievementRepository: AchievementRepository by lazy {
        AchievementRepository(achievementDao = database.achievementDao())
    }

    private val notificationsSettingsRepository: NotificationsSettingsRepository by lazy {
        NotificationsSettingsRepository(notificationsSettingsDao = database.notificationsSettingsDao())
    }

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(settingsDao = database.settingsDao())
    }

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("settings", MODE_PRIVATE)
    }

    override suspend fun doWork(): Result {

        val now: LocalDateTime = LocalDateTime.now()

        val lastHistory = historyRepository.getLast()
        val settings = settingsRepository.get()
        val notifications = notificationsSettingsRepository.get()

        var achievements = achievementRepository.getAll()

        if (lastHistory != null) {
            val achievementEvaluator = AchievementEvaluator(
                historyRepository = historyRepository,
                achievementRepository = achievementRepository
            )

            achievementEvaluator.evaluate(
                lastSmokeTime = lastHistory.createdAt,
                now = now
            )
            achievements = achievementRepository.getAll()
        }

        val duration: Duration? = lastHistory?.let {
            Duration.between(it.createdAt, now)
        }

        Notifications.createNotificationChannel(applicationContext)

        if (duration != null && notifications?.progress == true) {
            val frequency: ProgressFrequency = ProgressFrequency.fromValue(settings?.frequency)
            val intervalMillis: Long = when (frequency) {
                ProgressFrequency.HOURLY -> Duration.ofHours(1).toMillis()
                ProgressFrequency.DAILY -> Duration.ofDays(1).toMillis()
                ProgressFrequency.WEEKLY -> Duration.ofDays(7).toMillis()
            }

            val lastSentMillis: Long = sharedPreferences.getLong("last_progress_notification_time", 0L)
            val nowMillis: Long = System.currentTimeMillis()

            val shouldSend: Boolean = lastSentMillis == 0L || (nowMillis - lastSentMillis >= intervalMillis)

            if (shouldSend && duration.toMinutes() >= 1) {
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

                sharedPreferences.edit {
                    putLong("last_progress_notification_time", nowMillis)
                }
            }
        }

        achievements.forEach { achievement ->
            if (achievement.notify && notifications?.achievements == true && !achievement.reset) {

                val displayText = AchievementEntry
                    .fromEntity(entity = achievement)
                    .getDisplayText(applicationContext)

                val notificationContent: String = when (achievement.unit) {
                    AchievementUnit.CIGARETTES -> applicationContext.getString(
                        R.string.notification_achievement_unlocked_content_cigarettes,
                        displayText
                    )
                    else -> applicationContext.getString(
                        R.string.notification_achievement_unlocked_content_time,
                        displayText
                    )
                }

                Notifications.sendNotification(
                    context = applicationContext,
                    title = applicationContext.getString(R.string.notification_achievement_unlocked_title),
                    content = notificationContent,
                    notificationId = 1002 + achievement.id.toInt()
                )

                achievementRepository.update(
                    entry = achievement.copy(notify = false)
                )
            }
        }
        return Result.success()
    }
}