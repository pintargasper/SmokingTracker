package com.gasperpintar.smokingtracker.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.activity.MainActivity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.ui.container.ContainerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(value = AndroidJUnit4::class)
class HistoryAdapterTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun onBindViewHolderBindsDataAndHandlesClicksCorrectly() {
        activityScenarioRule.scenario.onActivity { activity ->
            var clickedEditEntry: HistoryEntry? = null
            var clickedDeleteEntry: HistoryEntry? = null

            val adapter = ContainerManager.createHistoryAdapter(
                context = activity,
                onEdit = { historyEntry -> clickedEditEntry = historyEntry },
                onDelete = { historyEntry -> clickedDeleteEntry = historyEntry }
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

            val viewHolder = adapter.createViewHolder(recyclerView, 0)
            adapter.bindViewHolder(viewHolder, 0)

            val itemView = viewHolder.itemView

            val timerLabel: TextView = itemView.findViewById(R.id.timer_label)
            val lent: View = itemView.findViewById(R.id.lent)
            val edit: View = itemView.findViewById(R.id.edit)
            val delete: View = itemView.findViewById(R.id.delete)

            assertEquals("00:10:00", timerLabel.text.toString())
            assertEquals(View.VISIBLE, lent.visibility)

            edit.performClick()
            assertSame(historyEntry, clickedEditEntry)

            delete.performClick()
            assertSame(historyEntry, clickedDeleteEntry)
        }
    }
}