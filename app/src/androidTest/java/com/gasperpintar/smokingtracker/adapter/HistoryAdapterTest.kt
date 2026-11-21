package com.gasperpintar.smokingtracker.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class HistoryAdapterTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun onBindViewHolderBindsDataAndHandlesClicksCorrectly() {
        var clickedEditEntry: HistoryEntry? = null
        var clickedDeleteEntry: HistoryEntry? = null

        val historyEntry = HistoryEntry(
            id = 1,
            isLent = true,
            createdAt = LocalDateTime.of(2025, 12, 31, 10, 0),
            timerLabel = "00:10:00"
        )

        activityScenarioRule.scenario.onActivity { activity ->
            val adapter = ContainerManager.createHistoryAdapter(
                context = activity,
                onEdit = { clickedEditEntry = it },
                onDelete = { clickedDeleteEntry = it }
            )

            RecyclerView(activity).apply {
                layoutManager = LinearLayoutManager(activity)
                this.adapter = adapter
            }.also { activity.setContentView(it) }
            adapter.submitList(listOf(historyEntry))
        }

        onView(withId(R.id.timer_label)).check(matches(withText("00:10:00")))
        onView(withId(R.id.lent)).check(matches(isDisplayed()))
        onView(withId(R.id.more)).perform(click())

        onView(withId(R.id.history_option_edit))
            .check(matches(isDisplayed()))
            .perform(click())

        assertSame(historyEntry, clickedEditEntry)

        onView(withId(R.id.more)).perform(click())

        onView(withId(R.id.history_option_delete))
            .check(matches(isDisplayed()))
            .perform(click())

        assertSame(historyEntry, clickedDeleteEntry)
    }
}