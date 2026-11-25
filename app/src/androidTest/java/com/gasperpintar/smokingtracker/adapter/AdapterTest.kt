package com.gasperpintar.smokingtracker.adapter

import androidx.viewbinding.ViewBinding
import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.ui.adapter.Adapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AdapterTest {

    private lateinit var adapter: Adapter<TestItem, ViewBinding>

    @Before
    fun setup() {
        adapter = Adapter(
            createViewHolder = { _, _ ->
                error("ViewHolder should not be created")
            },
            onBind = { _, _ -> }
        )
    }

    @Test
    fun submitListUpdatesAdapterItemCount() {
        submitListAndWait(items = listOf(createTestItem(id = 1), createTestItem(id = 2), createTestItem(id = 3)))

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun submitEmptyListSetsItemCountToZero() {
        submitListAndWait(items = emptyList())

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun submitNewListReplacesPreviousItems() {
        submitListAndWait(items = listOf(createTestItem(id = 1), createTestItem(id = 2)))
        submitListAndWait(items = listOf(createTestItem(id = 3)))

        assertEquals(1, adapter.itemCount)
        assertEquals(3L, adapter.currentList.first().id)
    }

    @Test
    fun submitListUpdatesExistingItem() {
        submitListAndWait(items = listOf(createTestItem(id = 1, value = "Old")))
        submitListAndWait(items = listOf(createTestItem(id = 1, value = "New")))

        assertEquals(1, adapter.itemCount)
        assertEquals("New", adapter.currentList.first().value)
    }

    @Test
    fun submitListRemovesMissingItems() {
        submitListAndWait(items = listOf(createTestItem(id = 1), createTestItem(id = 2), createTestItem(id = 3)))
        submitListAndWait(listOf(createTestItem(id = 1), createTestItem(id = 3)))

        assertEquals(2, adapter.itemCount)
        assertEquals(listOf(1L, 3L), adapter.currentList.map { it.id })
    }

    @Test
    fun currentListContainsSubmittedItems() {
        val items = listOf(createTestItem(id = 1), createTestItem(id = 2))
        submitListAndWait(items)

        assertEquals(items, adapter.currentList)
    }

    @Test
    fun currentListIsEmptyInitially() {
        assertTrue(adapter.currentList.isEmpty())
        assertEquals(0, adapter.itemCount)
    }

    private fun submitListAndWait(items: List<TestItem>) {
        val latch = CountDownLatch(1)

        adapter.submitList(items) {
            latch.countDown()
        }

        assertTrue("List submission did not complete in time", latch.await(5, TimeUnit.SECONDS))
    }

    private fun createTestItem(
        id: Long,
        value: String = "Test"
    ): TestItem {
        return TestItem(id = id, value = value)
    }

    private data class TestItem(
        @get:Override
        override val id: Long,
        val value: String
    ) : IIdentifiable
}