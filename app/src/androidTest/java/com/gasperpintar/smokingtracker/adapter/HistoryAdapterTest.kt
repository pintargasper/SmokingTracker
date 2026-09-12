package com.gasperpintar.smokingtracker.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.databinding.HistoryContainerBinding
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
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

            val adapter = Adapter<HistoryEntry, HistoryContainerBinding>(
                bindingFactory = HistoryContainerBinding::inflate,
                onBind = { historyEntry ->
                    timerLabel.text = historyEntry.timerLabel
                    lent.visibility = if (historyEntry.isLent) View.VISIBLE else View.GONE

                    edit.setOnClickListener {
                        clickedEditEntry = historyEntry
                    }

                    delete.setOnClickListener {
                        clickedDeleteEntry = historyEntry
                    }
                }
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

            val binding = HistoryContainerBinding.bind(viewHolder.itemView)

            assertEquals("00:10:00", binding.timerLabel.text.toString())
            assertEquals(View.VISIBLE, binding.lent.visibility)

            binding.edit.performClick()
            assertSame(historyEntry, clickedEditEntry)

            binding.delete.performClick()
            assertSame(historyEntry, clickedDeleteEntry)
        }
    }
}