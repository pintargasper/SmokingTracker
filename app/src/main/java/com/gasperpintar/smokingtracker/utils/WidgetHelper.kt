package com.gasperpintar.smokingtracker.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.provider.SmokingTrackerWidget
import com.gasperpintar.smokingtracker.repository.SettingsRepository

object WidgetHelper {

    const val ACTION_MIDNIGHT_WIDGET_UPDATE: String = "com.gasperpintar.smokingtracker.ACTION_MIDNIGHT_WIDGET_UPDATE"

    fun updateWidget(
        context: Context
    ) {
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, SmokingTrackerWidget::class.java)
        val appWidgetIds: IntArray = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) {
            return
        }

        val intent: Intent =
            Intent(context, SmokingTrackerWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
        context.sendBroadcast(intent)
    }

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleMidnightWidgetUpdate(
        context: Context
    ) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent: Intent = Intent(context, SmokingTrackerWidget::class.java).apply {
            action = ACTION_MIDNIGHT_WIDGET_UPDATE
        }

        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis: Long = TimeHelper.getNextMidnightMillis()
        val canUseExactAlarm: Boolean = Build.VERSION.SDK_INT <
                Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
        if (canUseExactAlarm) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun getString(
        context: Context,
        settingsRepository: SettingsRepository,
        resourceId: Int
    ): String {
        return LocalizationHelper
            .getLocalizedContext(context = context, settingsRepository = settingsRepository)
            .getString(resourceId)
    }
}

