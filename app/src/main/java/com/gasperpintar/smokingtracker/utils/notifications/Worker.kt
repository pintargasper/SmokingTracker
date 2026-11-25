package com.gasperpintar.smokingtracker.utils.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.type.Preferences
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementEvaluator
import com.gasperpintar.smokingtracker.utils.PreferencesHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.time.Duration
import java.time.LocalDateTime

class Worker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext = context, workerParams) {

    private val container by lazy {
        (applicationContext as Application).container
    }

    private val preferences by lazy {
        PreferencesHelper.with(context = applicationContext, name = Preferences.Name.SETTINGS)
    }

    @Override
    override suspend fun doWork(): Result {
        val now = LocalDateTime.now()

        val lastHistory = container.historyRepository.getLast()
        val settings = container.settingsRepository.get()
        val notifications = container.notificationsRepository.get()

        if (lastHistory != null) {
            AchievementEvaluator(
                context = applicationContext,
                historyRepository = container.historyRepository,
                achievementRepository = container.achievementRepository,
                notificationsRepository = container.notificationsRepository
            ).evaluate(lastSmokeTime = lastHistory.createdAt, now = now)
        }

        val duration = lastHistory?.let { Duration.between(it.createdAt, now) }
        if (duration != null && notifications?.progress == true) {
            checkAndSendProgressNotification(duration, settings?.frequency)
        }
        return Result.success()
    }

    private fun checkAndSendProgressNotification(duration: Duration, frequency: Int?) {
        val frequency = ProgressFrequency.fromValue(frequency)
        val intervalMillis = when (frequency) {
            ProgressFrequency.HOURLY -> Duration.ofHours(1).toMillis()
            ProgressFrequency.DAILY -> Duration.ofDays(1).toMillis()
            ProgressFrequency.WEEKLY -> Duration.ofDays(7).toMillis()
        }

        val lastSentMillis = preferences.get(key = Preferences.Key.LAST_PROGRESS_NOTIFICATION_TIME, defaultValue = 0L)
        val nowMillis = System.currentTimeMillis()

        val shouldSend = lastSentMillis == 0L || (nowMillis - lastSentMillis >= intervalMillis)
        if (shouldSend && duration.toHours() > 1) {
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
            preferences.put(Preferences.Key.LAST_PROGRESS_NOTIFICATION_TIME, nowMillis)
        }
    }
}