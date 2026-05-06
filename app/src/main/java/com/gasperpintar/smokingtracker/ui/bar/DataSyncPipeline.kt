package com.gasperpintar.smokingtracker.ui.bar

class DataSyncPipeline(
    private val steps: List<SyncedStep>
) {
    private var accumulatedWeight: Int = 0

    suspend fun run(onProgress: (Int) -> Unit) {
        val totalWeight = steps.sumOf {
            it.weight
        }

        steps.forEach { step ->
            val stepStartProgress = (accumulatedWeight.toFloat() / totalWeight * 100).toInt()
            val stepEndProgress = ((accumulatedWeight + step.weight).toFloat() / totalWeight * 100).toInt()

            step.action { innerProgress ->
                val mapped = stepStartProgress + ((stepEndProgress - stepStartProgress) * innerProgress / 100)
                onProgress(mapped)
            }
            accumulatedWeight += step.weight
        }
        onProgress(100)
    }
}