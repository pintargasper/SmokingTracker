package com.gasperpintar.smokingtracker.provider

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.repository.HistoryRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.TimeHelper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SmokingTrackerWidget : AppWidgetProvider() {

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)
        if (intent.action == WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE) {
            WidgetHelper.updateWidget(context = context)
            WidgetHelper.scheduleMidnightWidgetUpdate(context = context)
        }
    }

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        WidgetHelper.scheduleMidnightWidgetUpdate(context)
    }

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetHelper.scheduleMidnightWidgetUpdate(context)
    }

    companion object {

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_smoking_tracker)

            val database = Provider.getDatabase(context)
            val historyRepository = HistoryRepository(historyDao = database.historyDao())
            val settingsRepository = SettingsRepository(settingsDao = database.settingsDao())

            val today = LocalDate.now()
            val (startOfDay, endOfDay) = TimeHelper.getDay(date = today)
            val (startOfWeek, endOfWeek) = TimeHelper.getWeek(date = today)
            val (startOfMonth, endOfMonth) = TimeHelper.getMonth(date = today)

            openActivity(context, views)

            CoroutineScope(context = Dispatchers.IO).launch {
                try {
                    val daily = historyRepository.getCountBetween(start = startOfDay, end = endOfDay)
                    val weekly = historyRepository.getCountBetween(start = startOfWeek, end = endOfWeek)
                    val monthly = historyRepository.getCountBetween(start = startOfMonth, end = endOfMonth)

                    withContext(context = Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_daily_label, WidgetHelper.getString(context = context, settingsRepository = settingsRepository, resourceId = R.string.home_daily_label))
                        views.setTextViewText(R.id.widget_weekly_label, WidgetHelper.getString(context = context, settingsRepository = settingsRepository, resourceId = R.string.home_weekly_label))
                        views.setTextViewText(R.id.widget_monthly_label, WidgetHelper.getString(context = context, settingsRepository = settingsRepository, resourceId = R.string.home_monthly_label))
                        views.setTextViewText(R.id.widget_daily_value, daily.toString())
                        views.setTextViewText(R.id.widget_weekly_value, weekly.toString())
                        views.setTextViewText(R.id.widget_monthly_value, monthly.toString())
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (_: Exception) {}
            }
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
}