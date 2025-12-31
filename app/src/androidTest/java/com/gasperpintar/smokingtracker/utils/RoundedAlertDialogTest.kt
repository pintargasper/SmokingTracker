package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.gasperpintar.smokingtracker.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoundedAlertDialogTest {

    private lateinit var context: Context

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun creationOnUiThread() {
        activityScenarioRule.scenario.onActivity { activity ->
            val roundedAlertDialog = RoundedAlertDialog(context = activity)
            assertNotNull("Dialog should be instantiated", roundedAlertDialog)
        }
    }

    @Test
    fun showChainedReturnsSelf() {
        activityScenarioRule.scenario.onActivity { activity ->
            val roundedAlertDialog = RoundedAlertDialog(activity)
            val returnedDialog = roundedAlertDialog.showChained()
            assertSame(
                "showChained should return the same instance",
                roundedAlertDialog,
                returnedDialog
            )
        }
    }

    @Test
    fun setViewChainedReturnsSelf() {
        activityScenarioRule.scenario.onActivity { activity ->
            val roundedAlertDialog = RoundedAlertDialog(context = activity)
            val dummyView = View(activity)
            val returnedDialog = roundedAlertDialog.setViewChained(dummyView)
            assertSame(
                "setViewChained should return the same instance",
                roundedAlertDialog,
                returnedDialog
            )
        }
    }
}