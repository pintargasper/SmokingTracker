package com.gasperpintar.smokingtracker.adapter.history

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

@RunWith(value = AndroidJUnit4::class)
class HistoryAdapterTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun onBindViewHolder() {
        val clickedEditEntry = AtomicReference<HistoryEntry>()
        val clickedDeleteEntry = AtomicReference<HistoryEntry>()

        activityScenarioRule.scenario.onActivity { activity ->

            val adapter = HistoryAdapter(
                onEditClick = { clickedEditEntry.set(it) },
                onDeleteClick = { clickedDeleteEntry.set(it) }
            )

            val recyclerView = RecyclerView(activity).apply {
                layoutManager = LinearLayoutManager(activity)
                this.adapter = adapter
            }

            val historyEntry = HistoryEntry(
                id = 1,
                isLent = true,
                createdAt = LocalDateTime.of(2025, 12, 31, 10, 0),
                timerLabel = "00:10:00"
            )

            adapter.submitList(listOf(historyEntry))

            val viewHolder = recyclerView.findViewHolderForAdapterPosition(0) as? HistoryViewHolder
                ?: adapter.createViewHolder(recyclerView, 0).also { adapter.bindViewHolder(it, 0) }

            assertEquals("00:10:00", viewHolder.timerLabel.text.toString())
            assertEquals(View.VISIBLE, viewHolder.lentButton.visibility)

            viewHolder.editButton.performClick()
            assertSame(historyEntry, clickedEditEntry.get())

            viewHolder.deleteButton.performClick()
            assertSame(historyEntry, clickedDeleteEntry.get())
        }
    }
}