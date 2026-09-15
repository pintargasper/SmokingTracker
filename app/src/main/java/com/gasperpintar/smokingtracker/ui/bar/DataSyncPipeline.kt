package com.gasperpintar.smokingtracker.ui.bar

class DataSyncPipeline(private val steps: List<SyncedStep>) {

    suspend fun run(onProgress: (Int) -> Unit) {
        val totalWeight = steps.sumOf { it.weight.toDouble() }.takeIf { it > 0 } ?: return onProgress(100)
        var accumulatedWeight = 0.0

        steps.forEach { step ->
            val stepStartProgress = accumulatedWeight / totalWeight * 100
            val stepEndProgress = (accumulatedWeight + step.weight.toDouble()).also { accumulatedWeight = it } / totalWeight * 100
            step.action { innerProgress ->
                val mapped = stepStartProgress + (stepEndProgress - stepStartProgress) * innerProgress.toDouble() / 100
                onProgress(mapped.toInt().coerceIn(0, 100))
            }
        }
        onProgress(100)
    }
}