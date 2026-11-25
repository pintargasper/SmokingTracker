package com.gasperpintar.smokingtracker.ui.bar

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataSyncTest {

    @Test
    fun runMapsStepProgressByWeight() = runBlocking {
        val progress = mutableListOf<Int>()

        DataSync(
            steps = listOf(
                DataSync.SyncedStep(weight = 1) { it(100) },
                DataSync.SyncedStep(weight = 3) { it(50) }
            )
        ).run { progress += it }

        assertEquals(listOf(25, 62, 100), progress)
    }

    @Test
    fun runReportsMonotonicProgressWithinBounds() = runBlocking {
        val progress = mutableListOf<Int>()

        DataSync(
            steps = listOf(2, 5, 1).map { weight ->
                DataSync.SyncedStep(weight = weight) { onStepProgress ->
                    listOf(0, 50, 100).forEach(onStepProgress)
                }
            }
        ).run { progress += it }

        assertTrue(progress.all { it in 0..100 })
        assertTrue(progress.zipWithNext().all { (previous, current) -> current >= previous })
        assertEquals(100, progress.last())
    }

    @Test
    fun runExecutesStepsInOrder() = runBlocking {
        val executed = mutableListOf<String>()

        DataSync(
            steps = listOf("first", "second", "third").map { name ->
                DataSync.SyncedStep(weight = 1) { executed += name }
            }
        ).run {}

        assertEquals(listOf("first", "second", "third"), executed)
    }

    @Test
    fun runWithoutStepsReportsCompletion() = runBlocking {
        val progress = mutableListOf<Int>()

        DataSync(steps = emptyList()).run { progress += it }

        assertEquals(listOf(100), progress)
    }

    @Test
    fun runWithZeroWeightsSkipsStepsAndReportsCompletion() = runBlocking {
        val progress = mutableListOf<Int>()
        var executed = false

        DataSync(
            steps = listOf(DataSync.SyncedStep(weight = 0) { executed = true })
        ).run { progress += it }

        assertFalse(executed)
        assertEquals(listOf(100), progress)
    }
}