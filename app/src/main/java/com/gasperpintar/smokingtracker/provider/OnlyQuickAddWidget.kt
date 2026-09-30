package com.gasperpintar.smokingtracker.provider

import android.Manifest
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.utils.widget.WidgetHelper

class OnlyQuickAddWidget : AppWidgetProvider() {

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetHelper.updateWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            widgetClass = this.javaClass
        )
        WidgetHelper.scheduleMidnightWidgetUpdate(context)
    }

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    override fun onEnabled(
        context: Context
    ) {
        super.onEnabled(context)
        WidgetHelper.scheduleMidnightWidgetUpdate(context)
    }

    @Override
    override fun onDisabled(
        context: Context
    ) {
        super.onDisabled(context)
        WidgetHelper.cancelMidnightWidgetUpdate(context)
    }
}