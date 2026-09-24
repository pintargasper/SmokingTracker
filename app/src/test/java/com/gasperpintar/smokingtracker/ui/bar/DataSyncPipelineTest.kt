package com.gasperpintar.smokingtracker.ui.bar

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataSyncPipelineTest {

    @Test
    fun runMapsStepProgressByWeight() = runBlocking {
        val progress = mutableListOf<Int>()

        DataSyncPipeline(
            steps = listOf(
                SyncedStep(weight = 1) { it(100) },
                SyncedStep(weight = 3) { it(50) }
            )
        ).run { progress += it }

        assertEquals(listOf(25, 62, 100), progress)
    }

    @Test
    fun runReportsMonotonicProgressWithinBounds() = runBlocking {
        val progress = mutableListOf<Int>()

        DataSyncPipeline(
            steps = listOf(2, 5, 1).map { weight ->
                SyncedStep(weight = weight) { onStepProgress ->
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

        DataSyncPipeline(
            steps = listOf("first", "second", "third").map { name ->
                SyncedStep(weight = 1) { executed += name }
            }
        ).run {}

        assertEquals(listOf("first", "second", "third"), executed)
    }

    @Test
    fun runWithoutStepsReportsCompletion() = runBlocking {
        val progress = mutableListOf<Int>()

        DataSyncPipeline(steps = emptyList()).run { progress += it }

        assertEquals(listOf(100), progress)
    }

    @Test
    fun runWithZeroWeightsSkipsStepsAndReportsCompletion() = runBlocking {
        val progress = mutableListOf<Int>()
        var executed = false

        DataSyncPipeline(
            steps = listOf(SyncedStep(weight = 0) { executed = true })
        ).run { progress += it }

        assertFalse(executed)
        assertEquals(listOf(100), progress)
    }
}