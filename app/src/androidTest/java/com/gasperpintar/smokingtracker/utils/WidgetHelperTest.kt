package com.gasperpintar.smokingtracker.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.Application
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.provider.QuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsQuickAddWidget
import com.gasperpintar.smokingtracker.provider.StatsWidget
import com.gasperpintar.smokingtracker.provider.WidgetActionReceiver
import com.gasperpintar.smokingtracker.utils.widget.WidgetHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds

@RunWith(value = AndroidJUnit4::class)
class WidgetHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val container = (context.applicationContext as Application).container

    @Before
    fun setup() = runBlocking {
        container.settingsRepository.insert(SettingsEntity.default())
        removePendingIntent(action = WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE)
        removePendingIntent(action = WidgetHelper.ACTION_ADD_NEW_ENTRY)
    }

    @After
    fun teardown() {
        removePendingIntent(action = WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE)
        removePendingIntent(action = WidgetHelper.ACTION_ADD_NEW_ENTRY)
    }

    @Test
    fun scheduleMidnightWidgetUpdateCreatesPendingIntent() = runBlocking {
        WidgetHelper.scheduleMidnightWidgetUpdate(context)

        var pendingIntent: PendingIntent? = null
        repeat(times = 20) {
            pendingIntent = getPendingIntent(action = WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE)
            if (pendingIntent != null) return@repeat
            delay(duration = 100.milliseconds)
        }

        assertNotNull(pendingIntent)
    }

    @Test
    fun updateWidgetDoesNotCrashWithoutRegisteredWidgets() {
        assertNoException { WidgetHelper.updateWidget(context, widgetClass = StatsWidget::class.java) }
    }

    @Test
    fun updateStatsQuickAddWidgetDoesNotCrashWithoutRegisteredWidgets() {
        assertNoException { WidgetHelper.updateWidget(context, widgetClass = StatsQuickAddWidget::class.java) }
    }

    @Test
    fun updateQuickAddWidgetDoesNotCrashWithoutRegisteredWidgets() {
        assertNoException { WidgetHelper.updateWidget(context, widgetClass = QuickAddWidget::class.java) }
    }

    @Test
    fun updateAllWidgetsDoesNotCrashWithoutRegisteredWidgets() {
        assertNoException { WidgetHelper.updateAllWidgets(context) }
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithEmptyWidgetIds() {
        updateStatsWidget(appWidgetIds = intArrayOf(1))
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithoutWeeklyStats() {
        updateStatsWidget(appWidgetIds = intArrayOf(1), showWeekly = false, showMonthly = true)
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithoutMonthlyStats() {
        updateStatsWidget(appWidgetIds = intArrayOf(1), showWeekly = true, showMonthly = false)
    }

    @Test
    fun updateStatsWidgetDoesNotCrashWithoutWeeklyAndMonthlyStats() {
        updateStatsWidget(appWidgetIds = intArrayOf(), showWeekly = false, showMonthly = false)
    }

    private fun updateStatsWidget(
        appWidgetIds: IntArray,
        showWeekly: Boolean = true,
        showMonthly: Boolean = true
    ) {
        assertNoException {
            WidgetHelper.updateStatsWidget(
                context = context,
                appWidgetManager = AppWidgetManager.getInstance(context),
                appWidgetIds = appWidgetIds,
                layoutId = R.layout.widget_stats,
                showWeekly = showWeekly,
                showMonthly = showMonthly
            )
        }
    }

    private fun getPendingIntent(action: String): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            Intent(context, WidgetActionReceiver::class.java).apply { this.action = action },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

    private fun removePendingIntent(action: String) {
        getPendingIntent(action)?.cancel()
    }

    private fun assertNoException(block: () -> Unit) {
        assertNull(runCatching(block).exceptionOrNull())
    }
}