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
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.activity.MainActivity
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.provider.OnlyQuickAddWidget
import com.gasperpintar.smokingtracker.provider.QuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsQuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsWidget
import com.gasperpintar.smokingtracker.provider.WidgetReceiver
import com.gasperpintar.smokingtracker.utils.LocalizationHelper
import com.gasperpintar.smokingtracker.utils.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

object WidgetHelper {

    const val ACTION_MIDNIGHT_WIDGET_UPDATE = "com.gasperpintar.smokingtracker.ACTION_MIDNIGHT_WIDGET_UPDATE"
    const val ACTION_ADD_NEW_ENTRY = "com.gasperpintar.smokingtracker.ACTION_ADD_NEW_ENTRY"
    private const val ADD_ENTRY_DELAY = 1000L

    private val widgetLayouts = mapOf(
        StatsWidget::class.java to R.layout.widget_stats,
        StatsQuickAddWidget::class.java to R.layout.widget_stats_quick_add,
        QuickAddWidget::class.java to R.layout.widget_quick_add,
        OnlyQuickAddWidget::class.java to R.layout.widget_only_quick_add
    )

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleMidnightWidgetUpdate(
        context: Context
    ) {
        CoroutineScope(context = Dispatchers.IO).launch {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val pendingIntent = getBroadcastPendingIntent(context, ACTION_MIDNIGHT_WIDGET_UPDATE)

            val container = (context.applicationContext as Application).container
            val triggerAt = TimeHelper.getNextMidnightMillis(container.settingsRepository.get()!!.dayEndMinutes)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            else
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelMidnightWidgetUpdate(
        context: Context
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(getBroadcastPendingIntent(context, ACTION_MIDNIGHT_WIDGET_UPDATE))
    }

    fun updateWidget(
        context: Context,
        widgetClass: Class<*>
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, widgetClass))
        if (ids.isNotEmpty()) {
            context.sendBroadcast(
                Intent(context, widgetClass).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }
    }

    fun updateAllWidgets(
        context: Context
    ) {
        widgetLayouts.keys.forEach { updateWidget(context, widgetClass = it) }
    }

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetClass: Class<*>,
        showWeekly: Boolean = true,
        showMonthly: Boolean = true
    ) {
        val layoutId = widgetLayouts[widgetClass] ?: return
        val container = (context.applicationContext as Application).container

        if (widgetClass == OnlyQuickAddWidget::class.java) {
            appWidgetIds.forEach { id ->
                val views = RemoteViews(context.packageName, layoutId).apply {
                    setViewVisibility(R.id.widget_add_new_entry_button, View.VISIBLE)
                    setViewVisibility(R.id.widget_add_new_entry, View.GONE)
                    setOnClickPendingIntent(R.id.widget_add_new_entry_button, getBroadcastPendingIntent(context, ACTION_ADD_NEW_ENTRY))
                }
                appWidgetManager.updateAppWidget(id, views)
            }
            return
        }

        CoroutineScope(context = Dispatchers.IO).launch {
            try {
                val dayEndMinutes = container.settingsRepository.get()!!.dayEndMinutes
                val today = TimeHelper.dayDate(dayEndMinutes)

                val (dayStart, dayEnd) = TimeHelper.getDay(date = today, dayEndMinutes = dayEndMinutes)
                val (weekStart, weekEnd) = TimeHelper.getWeek(date = today, dayEndMinutes = dayEndMinutes)
                val (monthStart, monthEnd) = TimeHelper.getMonth(date = today, dayEndMinutes = dayEndMinutes)

                val repository = container.historyRepository
                val daily = repository.getCountBetween(dayStart, dayEnd)
                val weekly = if (showWeekly) repository.getCountBetween(weekStart, weekEnd) else null
                val monthly = if (showMonthly) repository.getCountBetween(monthStart, monthEnd) else null

                val localizedContext = LocalizationHelper.getLocalizedContext(context = context, settingsRepository = container.settingsRepository)
                val openAppIntent = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                appWidgetIds.forEach { id ->
                    val views = RemoteViews(context.packageName, layoutId).apply {
                        setTextViewText(R.id.widget_daily_label, localizedContext.getString(R.string.home_daily))
                        setTextViewText(R.id.widget_daily_value, daily.toString())

                        weekly?.let {
                            setTextViewText(R.id.widget_weekly_label, localizedContext.getString(R.string.home_weekly))
                            setTextViewText(R.id.widget_weekly_value, it.toString())
                        }

                        monthly?.let {
                            setTextViewText(R.id.widget_monthly_label, localizedContext.getString(R.string.home_monthly))
                            setTextViewText(R.id.widget_monthly_value, it.toString())
                        }

                        setTextViewText(R.id.widget_add_new_entry, localizedContext.getString(R.string.insert_popup))
                        setTextColor(R.id.widget_add_new_entry, ContextCompat.getColor(context, R.color.primary))
                        setBoolean(R.id.widget_add_new_entry, "setEnabled", true)

                        setOnClickPendingIntent(R.id.widget_add_new_entry, getBroadcastPendingIntent(context, ACTION_ADD_NEW_ENTRY))
                        setOnClickPendingIntent(R.id.widget_container, openAppIntent)
                    }
                    appWidgetManager.updateAppWidget(id, views)
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
                    if (ids.isEmpty()) return@forEach

                    RemoteViews(context.packageName, layoutId).apply {
                        setTextViewText(R.id.widget_add_new_entry, "✓")
                        setTextColor(R.id.widget_add_new_entry, ContextCompat.getColor(context, R.color.success))
                        setBoolean(R.id.widget_add_new_entry, "setEnabled", false)

                        if (widgetClass == OnlyQuickAddWidget::class.java) {
                            setViewVisibility(R.id.widget_add_new_entry_button, View.GONE)
                            setViewVisibility(R.id.widget_add_new_entry, View.VISIBLE)
                        }
                    }.also { manager.partiallyUpdateAppWidget(ids, it) }
                }
                delay(duration = ADD_ENTRY_DELAY.milliseconds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun getBroadcastPendingIntent(
        context: Context,
        action: String
    ): PendingIntent {
        val intent = Intent(action, null, context, WidgetReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}