package com.gasperpintar.smokingtracker.utils.widget

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
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.di.Container
import com.gasperpintar.smokingtracker.provider.QuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsQuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsWidget
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
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

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleMidnightWidgetUpdate(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            Intent(ACTION_MIDNIGHT_WIDGET_UPDATE, null, context, StatsWidget::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = TimeHelper.getNextMidnightMillis()
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        else alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancelMidnightWidgetUpdate(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(
            PendingIntent.getBroadcast(
            context,
            ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
                Intent(ACTION_MIDNIGHT_WIDGET_UPDATE, null, context, StatsWidget::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ))
    }

    fun updateWidget(
        context: Context,
        widgetClass: Class<*>
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, widgetClass))

        if (appWidgetIds.isNotEmpty()) {
            context.sendBroadcast(Intent(context, widgetClass).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            })
        }
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
        val container = getContainer(context)
        val today = LocalDate.now()

        val (dayStart, dayEnd) = TimeHelper.getDay(date = today)
        val (weekStart, weekEnd) = TimeHelper.getWeek(date = today)
        val (monthStart, monthEnd) = TimeHelper.getMonth(date = today)

        CoroutineScope(context = Dispatchers.IO).launch {
            try {
                val repository = container.historyRepository

                val stats = WidgetStats(
                    daily = repository.getCountBetween(dayStart, dayEnd),
                    weekly = if (showWeekly) repository.getCountBetween(weekStart, weekEnd) else null,
                    monthly = if (showMonthly) repository.getCountBetween(monthStart, monthEnd) else null
                )

                appWidgetIds.forEach { id ->
                    RemoteViews(context.packageName, layoutId).also { views ->
                        setStats(context, views, container.settingsRepository, stats)
                        newEntryClick(context, views)
                        openActivity(context, views)
                        appWidgetManager.updateAppWidget(id, views)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun addNewEntry(context: Context, pendingResult: BroadcastReceiver.PendingResult) {
        val container = getContainer(context)
        CoroutineScope(context = Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                if (now - lastEntryTime < 1000) return@launch
                lastEntryTime = now
                container.historyRepository.insert(entry = HistoryEntity(
                    id = 0L,
                    lent = 0,
                    createdAt = LocalDateTime.now()
                )
                )
                container.achievementRepository.resetAll(state = true)
                updateAllWidgets(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun getContainer(context: Context): Container {
        return (context.applicationContext as Application).container
    }

    private fun getString(
        context: Context,
        settingsRepository: SettingsRepository,
        resourceId: Int
    ): String {
        return LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository).getString(resourceId)
    }

    private fun setStats(
        context: Context,
        views: RemoteViews,
        settingsRepository: SettingsRepository,
        stats: WidgetStats
    ) {
        fun bind(labelRes: Int, labelId: Int, valueId: Int, value: Any?) {
            value?.let {
                views.setTextViewText(labelId, getString(context, settingsRepository, labelRes))
                views.setTextViewText(valueId, it.toString())
            }
        }
        bind(labelRes = R.string.home_daily, labelId = R.id.widget_daily_label, valueId = R.id.widget_daily_value, value = stats.daily)
        bind(labelRes = R.string.home_weekly, labelId = R.id.widget_weekly_label, valueId = R.id.widget_weekly_value, value = stats.weekly)
        bind(labelRes = R.string.home_monthly, labelId = R.id.widget_monthly_label, valueId = R.id.widget_monthly_value, value = stats.monthly)
    }

    private fun newEntryClick(context: Context, views: RemoteViews) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_ADD_NEW_ENTRY.hashCode(),
            Intent(context, StatsWidget::class.java).apply { action = ACTION_ADD_NEW_ENTRY },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_add_new_entry, pendingIntent)
    }

    private fun openActivity(context: Context, views: RemoteViews) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    }
}