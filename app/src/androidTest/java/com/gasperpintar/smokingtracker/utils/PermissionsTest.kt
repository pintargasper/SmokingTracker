package com.gasperpintar.smokingtracker.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.gasperpintar.smokingtracker.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class PermissionsTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun isNotificationPermissionGrantedReturnsCorrectState() {
        activityScenarioRule.scenario.moveToState(Lifecycle.State.CREATED)
        activityScenarioRule.scenario.onActivity { activity ->
            val permissions = Permissions(activity)

            val expected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            val actual = permissions.isNotificationPermissionGranted()

            assertEquals(expected, actual)
        }
    }


    @Test
    fun checkAndRequestNotificationPermissionReturnsTrueOnOlderAndroid() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return
        }

        var callbackResult: Boolean? = null

        activityScenarioRule.scenario.moveToState(Lifecycle.State.CREATED)
        activityScenarioRule.scenario.onActivity { activity ->
            val permissions = Permissions(activity)

            permissions.checkAndRequestNotificationPermission {
                callbackResult = it
            }
        }

        assertEquals(true, callbackResult)
    }

    @Test
    fun checkAndRequestNotificationPermissionReturnsTrueWhenAlreadyGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            "com.gasperpintar.smokingtracker",
            Manifest.permission.POST_NOTIFICATIONS
        )

        var callbackResult: Boolean? = null

        activityScenarioRule.scenario.moveToState(Lifecycle.State.CREATED)
        activityScenarioRule.scenario.onActivity { activity ->
            val permissions = Permissions(activity)

            val expected = PackageManager.PERMISSION_GRANTED
            val actual = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )

            assertEquals(expected, actual)

            permissions.checkAndRequestNotificationPermission {
                callbackResult = it
            }
        }

        assertEquals(true, callbackResult)
    }
}