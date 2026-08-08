package com.gasperpintar.smokingtracker.provider

import android.Manifest
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.utils.WidgetHelper

class StatsQuickAddWidget : AppWidgetProvider() {

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        WidgetHelper.updateStatsWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            layoutId = R.layout.widget_stats_quick_add
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