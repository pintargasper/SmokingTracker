package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.type.Preferences
import com.google.android.apps.common.testing.accessibility.framework.BuildConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class PreferencesHelperTest {

    private lateinit var context: Context
    private lateinit var preferencesHelper: PreferencesHelper.Instance

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        context.getSharedPreferences(Preferences.Name.SETTINGS.value, Context.MODE_PRIVATE).edit().clear().apply()
        preferencesHelper = PreferencesHelper.with(context = context, name = Preferences.Name.SETTINGS)
    }

    @After
    fun teardown() {
        context.getSharedPreferences(Preferences.Name.SETTINGS.value, Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun getReturnsDefaultValueWhenPreferenceDoesNotExist() {
        assertEquals("default", preferencesHelper.get(key = Preferences.Key.LAST_VERSION_NAME, defaultValue = "default"))
        assertEquals(false, preferencesHelper.get(key = Preferences.Key.FIRST_RUN, defaultValue = false))
        assertEquals(0, preferencesHelper.get(key = Preferences.Key.LAST_PROGRESS_NOTIFICATION_TIME, defaultValue = 0))
    }

    @Test
    fun putAndGetStringReturnsCorrectValue() {
        val expected = BuildConfig.VERSION_NAME

        preferencesHelper.put(key = Preferences.Key.LAST_VERSION_NAME, value = expected)
        val result = preferencesHelper.get(key = Preferences.Key.LAST_VERSION_NAME, defaultValue = "")

        assertEquals(expected, result)
    }

    @Test
    fun putAndGetBooleanReturnsCorrectValue() {
        val expected = true

        preferencesHelper.put(key = Preferences.Key.FIRST_RUN, value = expected)
        val result = preferencesHelper.get(key = Preferences.Key.FIRST_RUN, defaultValue = false)

        assertEquals(expected, result)
    }

    @Test
    fun putAndGetIntReturnsCorrectValue() {
        val expected = 42

        preferencesHelper.put(key = Preferences.Key.LAST_VERSION_NAME, value = expected)
        val result = preferencesHelper.get(key = Preferences.Key.LAST_VERSION_NAME, defaultValue = 0)

        assertEquals(expected, result)
    }

    @Test
    fun putAndGetLongReturnsCorrectValue() {
        val expected = 1735689600000L

        preferencesHelper.put(key = Preferences.Key.LAST_PROGRESS_NOTIFICATION_TIME, value = expected)
        val result = preferencesHelper.get(key = Preferences.Key.LAST_PROGRESS_NOTIFICATION_TIME, defaultValue = 0L)

        assertEquals(expected, result)
    }

    @Test
    fun putAndGetFloatReturnsCorrectValue() {
        val expected = 12.5f

        preferencesHelper.put(key = Preferences.Key.LAST_VERSION_NAME, value = expected)
        val result = preferencesHelper.get(key = Preferences.Key.LAST_VERSION_NAME, defaultValue = 0f)

        assertEquals(expected, result)
    }

    @Test
    fun withReturnsInstanceForProvidedPreferencesName() {
        val instance = PreferencesHelper.with(context = context, name = Preferences.Name.SETTINGS)

        instance.put(key = Preferences.Key.FIRST_RUN, value = true)

        assertEquals(true, instance.get(key = Preferences.Key.FIRST_RUN, defaultValue = false))
    }
}