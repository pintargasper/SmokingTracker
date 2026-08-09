package com.gasperpintar.smokingtracker.provider

import android.Manifest
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.utils.WidgetHelper

class StatsWidget : AppWidgetProvider() {

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)
        when (intent.action) {
            WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE -> {
                WidgetHelper.scheduleMidnightWidgetUpdate(context)
                WidgetHelper.updateAllWidgets(context)
            }

            WidgetHelper.ACTION_ADD_NEW_ENTRY -> {
                WidgetHelper.addNewEntry(context, pendingResult = goAsync())
            }
        }
    }

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetHelper.updateStatsWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            layoutId = R.layout.widget_stats
        )
        WidgetHelper.scheduleMidnightWidgetUpdate(context)
    }

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onEnabled(
        context: Context
    ) {
        super.onEnabled(context)
        WidgetHelper.scheduleMidnightWidgetUpdate(context)
    }
}