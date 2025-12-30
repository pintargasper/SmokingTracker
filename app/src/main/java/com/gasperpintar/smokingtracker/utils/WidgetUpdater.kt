package com.gasperpintar.smokingtracker.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.gasperpintar.smokingtracker.provider.SmokingTrackerWidget

object WidgetUpdater {

    fun updateWidget(context: Context) {
        val intent = Intent(context, SmokingTrackerWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, SmokingTrackerWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}

