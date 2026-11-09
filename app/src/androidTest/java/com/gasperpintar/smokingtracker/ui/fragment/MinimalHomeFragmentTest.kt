package com.gasperpintar.smokingtracker.ui.fragment

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.Provider
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.ElapsedTimeWarning
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class MinimalHomeFragmentTest {

    private lateinit var context: Context
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        val database = Provider.getDatabase(context)
        historyRepository = HistoryRepository(historyDao = database.historyDao())
        historyRepository.deleteAll()
        settingsRepository = SettingsRepository(settingsDao = database.settingsDao())
        settingsRepository.upsert(
            settingsRepository.get()?.copy(
                dayEndMinutes = 0,
                elapsedTimeWarningMinutes = ElapsedTimeWarning.DEFAULT_MINUTES
            ) ?: SettingsEntity.default().copy(dayEndMinutes = 0)
        )
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("first_run", false)
            .commit()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        instrumentation.uiAutomation
            .executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
            .close()
    }

    @Test
    fun oneTapAddsOneNonLentEntryAndUpdatesCounter() {
        val before = LocalDateTime.now().minusSeconds(2)
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.today_count)).check(matches(withText("0")))
            onView(withId(R.id.button_add_cigarette)).perform(click())

            val entries = awaitEntries()

            assertEquals(1, entries.size)
            assertEquals(0, entries.single().lent)
            assertTrue(entries.single().createdAt >= before)
            assertTrue(entries.single().createdAt <= LocalDateTime.now().plusSeconds(2))
            onView(withId(R.id.today_count)).check(matches(withText("1")))
        }
    }

    @Test
    fun minimalHomeIsFirstAndAdvancedRemainsAvailable() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(withId(R.id.timer_label)).check(matches(isDisplayed()))
            onView(withId(R.id.button_add_cigarette)).check(matches(isDisplayed()))
            onView(withId(R.id.today_count)).check(matches(isDisplayed()))

            scenario.onActivity { activity ->
                val menu = activity.findViewById<BottomNavigationView>(R.id.nav_view).menu
                val actualIds = (0 until menu.size()).map { menu.getItem(it).itemId }
                assertEquals(
                    listOf(
                        R.id.navigation_minimal_home,
                        R.id.navigation_home,
                        R.id.navigation_graph,
                        R.id.navigation_more,
                        R.id.navigation_settings
                    ),
                    actualIds
                )
            }

            onView(withId(R.id.navigation_home)).perform(click())
            onView(withId(R.id.button_add_entry)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun elapsedTimeThresholdSupportsDisablingAndPersistsValidValue() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.navigation_settings)).perform(click())
            onView(withId(R.id.elapsed_time_warning_layout)).perform(scrollTo(), click())

            onView(withId(R.id.input_warning_minutes)).perform(replaceText("10081"), closeSoftKeyboard())
            onView(withId(R.id.confirm)).perform(click())
            onView(withId(R.id.input_warning_minutes)).check(matches(isDisplayed()))
            assertEquals(60, runBlocking { settingsRepository.get()!!.elapsedTimeWarningMinutes })

            onView(withId(R.id.input_warning_minutes)).perform(replaceText("0"), closeSoftKeyboard())
            onView(withId(R.id.confirm)).perform(click())
            assertEquals(0, awaitElapsedTimeWarning(expected = 0))
            onView(withId(R.id.elapsed_time_warning_value)).perform(scrollTo()).check(matches(withText(
                context.resources.getQuantityString(R.plurals.time_minutes, 0, 0)
            )))

            onView(withId(R.id.elapsed_time_warning_layout)).perform(scrollTo(), click())
            onView(withId(R.id.input_warning_minutes)).perform(replaceText("120"), closeSoftKeyboard())
            onView(withId(R.id.confirm)).perform(click())

            assertEquals(120, awaitElapsedTimeWarning(expected = 120))
            onView(withId(R.id.elapsed_time_warning_value)).perform(scrollTo()).check(matches(withText(
                context.resources.getQuantityString(R.plurals.time_minutes, 120, 120)
            )))
        }
    }

    private fun awaitEntries(): List<HistoryEntity> {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val entries = runBlocking { historyRepository.getAll() }
            if (entries.isNotEmpty()) return entries
            Thread.sleep(100)
        }
        return runBlocking { historyRepository.getAll() }
    }

    private fun awaitElapsedTimeWarning(expected: Int): Int? {
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            val actual = runBlocking { settingsRepository.get()?.elapsedTimeWarningMinutes }
            if (actual == expected) return actual
            Thread.sleep(100)
        }
        return runBlocking { settingsRepository.get()?.elapsedTimeWarningMinutes }
    }
}