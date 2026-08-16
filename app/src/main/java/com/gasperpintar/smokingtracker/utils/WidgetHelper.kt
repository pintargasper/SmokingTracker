package com.gasperpintar.smokingtracker.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.provider.QuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsQuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsWidget
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

object WidgetHelper {

    const val ACTION_MIDNIGHT_WIDGET_UPDATE = "com.gasperpintar.smokingtracker.ACTION_MIDNIGHT_WIDGET_UPDATE"
    const val ACTION_ADD_NEW_ENTRY = "com.gasperpintar.smokingtracker.ACTION_ADD_NEW_ENTRY"

    private var lastEntryTime = 0L

    private val widgetClasses = listOf(
        StatsWidget::class.java,
        StatsQuickAddWidget::class.java,
        QuickAddWidget::class.java
    )

    private data class WidgetStats(
        val daily: Int,
        val weekly: Int? = null,
        val monthly: Int? = null
    )

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleMidnightWidgetUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, StatsWidget::class.java).apply {
            action = ACTION_MIDNIGHT_WIDGET_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = TimeHelper.getNextMidnightMillis()
        val canUseExactAlarm = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        when {
            canUseExactAlarm -> alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )

            else -> alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun updateWidget(
        context: Context,
        widgetClass: Class<*>
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, widgetClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) {
            return
        }

        val intent = Intent(context, widgetClass).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                appWidgetIds
            )
        }
        context.sendBroadcast(intent)
    }

    fun updateAllWidgets(context: Context) {
        widgetClasses.forEach {
            updateWidget(context, widgetClass = it)
        }
    }

    fun updateStatsWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        layoutId: Int,
        showWeekly: Boolean = true,
        showMonthly: Boolean = true
    ) {
        val database = Provider.getDatabase(context)
        val historyRepository = HistoryRepository(historyDao = database.historyDao())
        val settingsRepository = SettingsRepository(settingsDao = database.settingsDao())

        val today = LocalDate.now()
        val (startOfDay, endOfDay) = TimeHelper.getDay(date = today)
        val (startOfWeek, endOfWeek) = TimeHelper.getWeek(date = today)
        val (startOfMonth, endOfMonth) = TimeHelper.getMonth(date = today)

        CoroutineScope(context = Dispatchers.IO).launch {
            try {
                val stats = WidgetStats(
                    daily = historyRepository.getCountBetween(start = startOfDay, end = endOfDay),
                    weekly = showWeekly.takeIf { it }?.let {
                        historyRepository.getCountBetween(start = startOfWeek, end = endOfWeek)
                    },
                    monthly = showMonthly.takeIf { it }?.let {
                        historyRepository.getCountBetween(start = startOfMonth, end = endOfMonth)
                    }
                )

                appWidgetIds.forEach { appWidgetId ->
                    val views = RemoteViews(context.packageName, layoutId)

                    setStats(
                        context = context,
                        views = views,
                        settingsRepository = settingsRepository,
                        stats = stats
                    )

                    newEntryClick(context = context, views = views)
                    openActivity(context = context, views = views)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun addNewEntry(context: Context, pendingResult: BroadcastReceiver.PendingResult) {
        val database = Provider.getDatabase(context)
        val historyRepository = HistoryRepository(historyDao = database.historyDao())

        CoroutineScope(context = Dispatchers.IO).launch {
            try {

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastEntryTime < 1000) {
                    return@launch
                }
                lastEntryTime = currentTime

                historyRepository.insert(
                    entry = HistoryEntity(id = 0L, lent = 0, createdAt = LocalDateTime.now())
                )
                updateAllWidgets(context)
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun getString(
        context: Context,
        settingsRepository: SettingsRepository,
        resourceId: Int
    ): String {
        return LocalizationHelper.getLocalizedContext(
            context = context,
            settingsRepository = settingsRepository
        ).getString(resourceId)
    }

    private fun setStats(
        context: Context,
        views: RemoteViews,
        settingsRepository: SettingsRepository,
        stats: WidgetStats
    ) {
        views.setTextViewText(
            R.id.widget_daily_label,
            getString(
                context = context,
                settingsRepository = settingsRepository,
                resourceId = R.string.home_daily
            )
        )

        views.setTextViewText(R.id.widget_daily_value, stats.daily.toString())

        stats.weekly?.let { weekly ->
            views.setTextViewText(
                R.id.widget_weekly_label,
                getString(
                    context = context,
                    settingsRepository = settingsRepository,
                    resourceId = R.string.home_weekly
                )
            )
            views.setTextViewText(R.id.widget_weekly_value, weekly.toString())
        }

        stats.monthly?.let { monthly ->
            views.setTextViewText(
                R.id.widget_monthly_label,
                getString(
                    context = context,
                    settingsRepository = settingsRepository,
                    resourceId = R.string.home_monthly
                )
            )
            views.setTextViewText(R.id.widget_monthly_value, monthly.toString())
        }
    }

    private fun newEntryClick(
        context: Context,
        views: RemoteViews
    ) {
        val intent = Intent(context, StatsWidget::class.java).apply {
            action = ACTION_ADD_NEW_ENTRY
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_ADD_NEW_ENTRY.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(
            R.id.widget_add_new_entry,
            pendingIntent
        )
    }

    private fun openActivity(
        context: Context,
        views: RemoteViews
    ) {
        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    }
}