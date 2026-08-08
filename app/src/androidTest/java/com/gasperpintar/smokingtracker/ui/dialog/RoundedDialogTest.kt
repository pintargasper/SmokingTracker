package com.gasperpintar.smokingtracker.ui.dialog

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.MainActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class RoundedDialogTest {

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
            val roundedDialog = RoundedDialog(context = activity)
            Assert.assertNotNull("Dialog should be instantiated", roundedDialog)
        }
    }

    @Test
    fun showChainedReturnsSelf() {
        activityScenarioRule.scenario.onActivity { activity ->
            val roundedDialog = RoundedDialog(activity)
            val returnedDialog = roundedDialog.showChained()
            Assert.assertSame(
                "ShowChained should return the same instance",
                roundedDialog,
                returnedDialog
            )
        }
    }

    @Test
    fun setViewChainedReturnsSelf() {
        activityScenarioRule.scenario.onActivity { activity ->
            val roundedDialog = RoundedDialog(context = activity)
            val dummyView = View(activity)
            val returnedDialog = roundedDialog.setViewChained(dummyView)
            Assert.assertSame(
                "SetViewChained should return the same instance",
                roundedDialog,
                returnedDialog
            )
        }
    }
}