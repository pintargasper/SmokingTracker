package com.gasperpintar.smokingtracker.ui.dialog

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class RoundedDialogTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun dialogCanBeShown() {
        activityScenarioRule.scenario.onActivity { activity ->
            val dialog = RoundedDialog(context = activity)
            dialog.show()

            assertNotNull(dialog.window)
            dialog.dismiss()
        }
    }
}