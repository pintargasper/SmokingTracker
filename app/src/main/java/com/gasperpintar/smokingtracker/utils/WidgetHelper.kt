package com.gasperpintar.smokingtracker.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.gasperpintar.smokingtracker.provider.SmokingTrackerWidget
import com.gasperpintar.smokingtracker.repository.SettingsRepository

object WidgetHelper {

    fun updateWidget(context: Context) {
        val intent = Intent(context, SmokingTrackerWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, SmokingTrackerWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    fun getString(context: Context, settingsRepository: SettingsRepository, resId: Int): String {
        return LocalizationHelper.getLocalizedContext(context = context, settingsRepository = settingsRepository).getString(resId)
    }
}

