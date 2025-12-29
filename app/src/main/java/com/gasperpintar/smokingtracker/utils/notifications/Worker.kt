package com.gasperpintar.smokingtracker.utils.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.utils.Helper
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class Worker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext = context, workerParams) {

    private val database: AppDatabase by lazy { Provider.getDatabase(applicationContext) }

    @RequiresPermission(value = Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        Notifications.createNotificationChannel(applicationContext)

        val (_, endOfDay) = Helper.getDay(date = LocalDate.now())
        val lastHistory: HistoryEntity? = database.historyDao().getLastHistoryEntry(endOfToday = endOfDay)
        val settings: SettingsEntity? = database.settingsDao().getSettings()

        val timeSinceLastCigarette = lastHistory?.let {
            Duration.between(it.createdAt, LocalDateTime.now())
        }

        if (timeSinceLastCigarette != null &&
            timeSinceLastCigarette > Duration.ofMinutes(45) &&
            settings?.notifications == 1
        ) {
            val timeString = timeSinceLastCigarette.toReadableString()

            Notifications.sendNotification(
                context = applicationContext,
                title = applicationContext.getString(R.string.notification_title),
                content = applicationContext.getString(
                    R.string.notification_content,
                    timeString
                )
            )
        }
        return Result.success()
    }

    @SuppressLint("DefaultLocale")
    private fun Duration.toReadableString(): String {
        val hours = this.toHours()
        val minutes = this.toMinutes() % 60
        val seconds = this.seconds % 60

        return if (hours > 0) {
            String.format(
                $$"%1$dh %2$dm %3$ds",
                hours,
                minutes,
                seconds
            )
        } else {
            String.format(
                $$"%1$dm %2$ds",
                minutes,
                seconds
            )
        }
    }
}