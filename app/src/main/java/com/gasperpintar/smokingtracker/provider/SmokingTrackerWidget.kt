package com.gasperpintar.smokingtracker.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.utils.Helper
import com.gasperpintar.smokingtracker.utils.WidgetHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SmokingTrackerWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_smoking_tracker)
            val database = Provider.getDatabase(context = context)

            val today = LocalDate.now()
            val (startOfDay, endOfDay) = Helper.getDay(date = today)
            val (startOfWeek, endOfWeek) = Helper.getWeek(date = today)
            val (startOfMonth, endOfMonth) = Helper.getMonth(date = today)

            openActivity(context, views)

            CoroutineScope(context = Dispatchers.IO).launch {
                try {
                    val daily = database.historyDao().getHistoryCountBetween(startOfDay, endOfDay)
                    val weekly = database.historyDao().getHistoryCountBetween(startOfWeek, endOfWeek)
                    val monthly = database.historyDao().getHistoryCountBetween(startOfMonth, endOfMonth)

                    withContext(context = Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_daily_label, WidgetHelper.getString(context, R.string.home_daily_label))
                        views.setTextViewText(R.id.widget_weekly_label, WidgetHelper.getString(context, R.string.home_weekly_label))
                        views.setTextViewText(R.id.widget_monthly_label, WidgetHelper.getString(context, R.string.home_monthly_label))
                        views.setTextViewText(R.id.widget_daily_value, daily.toString())
                        views.setTextViewText(R.id.widget_weekly_value, weekly.toString())
                        views.setTextViewText(R.id.widget_monthly_value, monthly.toString())
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (_: Exception) {}
            }
        }

        private fun openActivity(context: Context, views: RemoteViews) {
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