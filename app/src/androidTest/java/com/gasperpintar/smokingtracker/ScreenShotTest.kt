package com.gasperpintar.smokingtracker

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(value = AndroidJUnit4::class)
class ScreenshotTest {

    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()
    }

    @Test
    fun captureAllScreenshots() {
        ActivityScenario.launch(MainActivity::class.java).use {
            Screengrab.screenshot("1")
        }

        ActivityScenario.launch(StatisticsActivity::class.java).use {
            Screengrab.screenshot("2")
        }

        ActivityScenario.launch(CalculatorActivity::class.java).use {
            Screengrab.screenshot("3")
        }

        ActivityScenario.launch(AchievementsActivity::class.java).use {
            Screengrab.screenshot("4")
        }

        ActivityScenario.launch(NotesActivity::class.java).use {
            Screengrab.screenshot("5")
        }

        ActivityScenario.launch(AboutActivity::class.java).use {
            Screengrab.screenshot("6")
        }
    }
}