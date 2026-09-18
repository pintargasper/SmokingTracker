package com.gasperpintar.smokingtracker.provider

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.gasperpintar.smokingtracker.utils.widget.WidgetHelper

class WidgetReceiver : BroadcastReceiver() {

    @RequiresPermission(value = Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        when (intent.action) {
            WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE -> {
                WidgetHelper.scheduleMidnightWidgetUpdate(context)
                WidgetHelper.updateAllWidgets(context)
            }

            WidgetHelper.ACTION_ADD_NEW_ENTRY -> {
                WidgetHelper.addNewEntry(context = context, pendingResult = goAsync())
            }
        }
    }
}