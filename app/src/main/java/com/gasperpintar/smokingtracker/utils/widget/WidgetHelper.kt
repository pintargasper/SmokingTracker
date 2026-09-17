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
import androidx.core.content.ContextCompat
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.activity.MainActivity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.provider.QuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsQuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsWidget
import com.gasperpintar.smokingtracker.provider.WidgetActionReceiver
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

object WidgetHelper {

    const val ACTION_MIDNIGHT_WIDGET_UPDATE = "com.gasperpintar.smokingtracker.ACTION_MIDNIGHT_WIDGET_UPDATE"
    const val ACTION_ADD_NEW_ENTRY = "com.gasperpintar.smokingtracker.ACTION_ADD_NEW_ENTRY"
    private const val ADD_ENTRY_DELAY = 1000L

    private val widgetClasses = listOf(
        StatsWidget::class.java,
        StatsQuickAddWidget::class.java,
        QuickAddWidget::class.java
    )

    private val widgetLayouts = mapOf(
        StatsWidget::class.java to R.layout.widget_stats,
        StatsQuickAddWidget::class.java to R.layout.widget_stats_quick_add,
        QuickAddWidget::class.java to R.layout.widget_quick_add
    )

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleMidnightWidgetUpdate(
        context: Context
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            Intent(ACTION_MIDNIGHT_WIDGET_UPDATE, null, context, WidgetActionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = TimeHelper.getNextMidnightMillis()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        else alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun cancelMidnightWidgetUpdate(
        context: Context
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            Intent(ACTION_MIDNIGHT_WIDGET_UPDATE, null, context, WidgetActionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun updateWidget(
        context: Context, widgetClass: Class<*>
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, widgetClass))
        if (ids.isEmpty()) return

        context.sendBroadcast(
            Intent(context, widgetClass).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
        )
    }

    fun updateAllWidgets(
        context: Context
    ) {
        widgetClasses.forEach { updateWidget(context, widgetClass = it) }
    }

    fun updateStatsWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        layoutId: Int,
        showWeekly: Boolean = true,
        showMonthly: Boolean = true
    ) {
        val container = (context.applicationContext as Application).container
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

                val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = container.settingsRepository)
                val addEntryIntent = PendingIntent.getBroadcast(
                    context,
                    ACTION_ADD_NEW_ENTRY.hashCode(),
                    Intent(context, WidgetActionReceiver::class.java).apply {
                        action = ACTION_ADD_NEW_ENTRY
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val openAppIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                appWidgetIds.forEach { id ->
                    RemoteViews(context.packageName, layoutId).apply {
                        setTextViewText(R.id.widget_daily_label, localizedContext.getString(R.string.home_daily))
                        setTextViewText(R.id.widget_daily_value, stats.daily.toString())

                        stats.weekly?.let {
                            setTextViewText(R.id.widget_weekly_label, localizedContext.getString(R.string.home_weekly))
                            setTextViewText(R.id.widget_weekly_value, it.toString())
                        }

                        stats.monthly?.let {
                            setTextViewText(R.id.widget_monthly_label, localizedContext.getString(R.string.home_monthly))
                            setTextViewText(R.id.widget_monthly_value, it.toString())
                        }

                        setTextViewText(R.id.widget_add_new_entry, localizedContext.getString(R.string.insert_popup))
                        setTextColor(R.id.widget_add_new_entry, ContextCompat.getColor(context, R.color.primary))
                        setBoolean(R.id.widget_add_new_entry, "setEnabled", true)

                        setOnClickPendingIntent(R.id.widget_add_new_entry, addEntryIntent)
                        setOnClickPendingIntent(R.id.widget_container, openAppIntent)
                        appWidgetManager.updateAppWidget(id, this)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun addNewEntry(
        context: Context,
        pendingResult: BroadcastReceiver.PendingResult
    ) {
        CoroutineScope(context = Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as Application).container
                container.historyRepository.insert(entry = HistoryEntity.default(isLent = false))
                container.achievementRepository.resetAll(state = true)
                updateAllWidgets(context)

                val manager = AppWidgetManager.getInstance(context)
                widgetLayouts.forEach { (widgetClass, layoutId) ->
                    val ids = manager.getAppWidgetIds(ComponentName(context, widgetClass))
                    if (ids.isNotEmpty()) {
                        RemoteViews(context.packageName, layoutId).apply {
                            setTextViewText(R.id.widget_add_new_entry, "✓")
                            setTextColor(R.id.widget_add_new_entry, ContextCompat.getColor(context, R.color.success))
                            setBoolean(R.id.widget_add_new_entry, "setEnabled", false)
                        }.also { manager.partiallyUpdateAppWidget(ids, it) }
                    }
                }
                delay(duration = ADD_ENTRY_DELAY.milliseconds)
            } finally {
                pendingResult.finish()
            }
        }
    }
}