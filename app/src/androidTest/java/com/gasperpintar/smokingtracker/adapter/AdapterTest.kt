package com.gasperpintar.smokingtracker.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.MainActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

@RunWith(value = AndroidJUnit4::class)
class AdapterTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun onBindViewHolder_bindsDataAndHandlesClicksCorrectly() {
        val clickedEditEntry = AtomicReference<HistoryEntry?>()
        val clickedDeleteEntry = AtomicReference<HistoryEntry?>()

        activityScenarioRule.scenario.onActivity { activity ->

            val adapter = Adapter(
                layoutId = R.layout.history_container,
                onBind = { itemView, historyEntry ->

                    val timerLabel =
                        itemView.findViewById<TextView>(R.id.timer_label)
                    val lentButton =
                        itemView.findViewById<ImageButton>(R.id.lent)
                    val editButton =
                        itemView.findViewById<ImageButton>(R.id.image_button_edit)
                    val deleteButton =
                        itemView.findViewById<ImageButton>(R.id.delete)

                    timerLabel.text = historyEntry.timerLabel
                    lentButton.visibility =
                        if (historyEntry.isLent) View.VISIBLE else View.GONE

                    editButton.setOnClickListener {
                        clickedEditEntry.set(historyEntry)
                    }

                    deleteButton.setOnClickListener {
                        clickedDeleteEntry.set(historyEntry)
                    }
                },
                diffCallback = object : DiffUtil.ItemCallback<HistoryEntry>() {
                    override fun areItemsTheSame(
                        oldItem: HistoryEntry,
                        newItem: HistoryEntry
                    ): Boolean = oldItem.id == newItem.id

                    override fun areContentsTheSame(
                        oldItem: HistoryEntry,
                        newItem: HistoryEntry
                    ): Boolean = oldItem == newItem
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

            val viewHolder =
                adapter.createViewHolder(recyclerView, 0).also {
                    adapter.bindViewHolder(it, 0)
                }

            val itemView = viewHolder.itemView

            val timerLabel = itemView.findViewById<TextView>(R.id.timer_label)
            val lentButton = itemView.findViewById<ImageButton>(R.id.lent)
            val editButton = itemView.findViewById<ImageButton>(R.id.image_button_edit)
            val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)

            assertEquals("00:10:00", timerLabel.text.toString())
            assertEquals(View.VISIBLE, lentButton.visibility)

            editButton.performClick()
            assertSame(historyEntry, clickedEditEntry.get())

            deleteButton.performClick()
            assertSame(historyEntry, clickedDeleteEntry.get())
        }
    }
}