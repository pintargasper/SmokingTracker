package com.gasperpintar.smokingtracker.utils

import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.TestProvider
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.provider.SmokingTrackerWidget
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class WidgetHelperTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var settingRepository: SettingsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = TestProvider.getInMemoryDatabase(context)
        settingRepository = SettingsRepository(settingsDao = database.settingsDao())
    }

    @After
    fun teardown() {
        TestProvider.closeDatabase()
    }

    @Test
    fun updateWidgetDoesNotCrashWithoutRegisteredWidgets() {
        val exception = runCatching {
            WidgetHelper.updateWidget(context)
        }.exceptionOrNull()

        assertTrue(exception == null)
    }

    @Test
    fun scheduleMidnightWidgetUpdateCreatesAlarm() {
        WidgetHelper.scheduleMidnightWidgetUpdate(context)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE.hashCode(),
            Intent(
                context,
                SmokingTrackerWidget::class.java
            ).apply {
                action = WidgetHelper.ACTION_MIDNIGHT_WIDGET_UPDATE
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        assertNotNull(pendingIntent)
    }

    @Test
    fun getStringReturnsLocalizedString() = runBlocking {
        settingRepository.update(
            SettingsEntity(
                id = 1,
                theme = 0,
                language = 0,
                frequency = 1,
                currency = "€",
                customCurrency = ""
            )
        )

        val localizedContext = LocalizationHelper.getLocalizedContext(
            context = context,
            settingsRepository = settingRepository
        )

        val expected = localizedContext.getString(R.string.ok)
        val actual = WidgetHelper.getString(
            context = context,
            settingsRepository = settingRepository,
            resourceId = R.string.ok
        )

        assertEquals(expected, actual)
    }
}