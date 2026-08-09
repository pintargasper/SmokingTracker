package com.gasperpintar.smokingtracker.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.provider.QuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsQuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsWidget
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class WidgetHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        removeMidnightPendingIntent()
        removeAddNewEntryPendingIntent()
    }

    @After
    fun teardown() {
        removeMidnightPendingIntent()
        removeAddNewEntryPendingIntent()
        TestProvider.closeDatabase()
    }

    @Test
    fun scheduleMidnightWidgetUpdateCreatesPendingIntent() {
        WidgetHelper.scheduleMidnightWidgetUpdate(context)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            Intent(
                context,
                StatsWidget::class.java
            ).apply {
                action = WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        assertNotNull(pendingIntent)
    }

    @Test
    fun updateWidgetDoesNotCrashWithoutRegisteredWidgets() {
        val exception = runCatching {
            WidgetHelper.updateWidget(
                context = context,
                widgetClass = StatsWidget::class.java
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateStatsQuickAddWidgetClassDoesNotCrashWithoutRegisteredWidgets() {
        val exception = runCatching {
            WidgetHelper.updateWidget(
                context = context,
                widgetClass = StatsQuickAddWidget::class.java
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateQuickAddWidgetClassDoesNotCrashWithoutRegisteredWidgets() {
        val exception = runCatching {
            WidgetHelper.updateWidget(
                context = context,
                widgetClass = QuickAddWidget::class.java
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateAllWidgetsDoesNotCrashWithoutRegisteredWidgets() {
        val exception = runCatching {
            WidgetHelper.updateAllWidgets(context)
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithEmptyWidgetIds() {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val exception = runCatching {
            WidgetHelper.updateStatsWidget(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = intArrayOf(1),
                layoutId = R.layout.widget_stats
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithoutWeeklyStats() {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val exception = runCatching {
            WidgetHelper.updateStatsWidget(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = intArrayOf(1),
                layoutId = R.layout.widget_stats,
                showWeekly = false,
                showMonthly = true
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithoutMonthlyStats() {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val exception = runCatching {
            WidgetHelper.updateStatsWidget(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = intArrayOf(1),
                layoutId = R.layout.widget_stats,
                showWeekly = true,
                showMonthly = false
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithoutWeeklyAndMonthlyStats() {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val exception = runCatching {
            WidgetHelper.updateStatsWidget(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = intArrayOf(),
                layoutId = R.layout.widget_stats,
                showWeekly = false,
                showMonthly = false
            )
        }.exceptionOrNull()

        assertNull(exception)
    }

    private fun removeMidnightPendingIntent() {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            Intent(
                context,
                StatsWidget::class.java
            ).apply {
                action = WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.cancel()
    }

    private fun removeAddNewEntryPendingIntent() {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WidgetHelper.ACTION_ADD_NEW_ENTRY.hashCode(),
            Intent(
                context,
                StatsWidget::class.java
            ).apply {
                action = WidgetHelper.ACTION_ADD_NEW_ENTRY
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.cancel()
    }
}