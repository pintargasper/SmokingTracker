package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.model.TriggerEntry
import com.gasperpintar.smokingtracker.database.repository.HistoryRepository
import com.gasperpintar.smokingtracker.database.repository.TriggerRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.TriggerState
import com.gasperpintar.smokingtracker.model.TriggerJson

class TriggerViewModel(
    private val triggerRepository: TriggerRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    suspend fun getState(): TriggerState {
        val triggers = triggerRepository.getAll().toMutableList()
        val smokedHistory = historyRepository.getAll().filter { !it.isLent }

        if (triggers.none { it.type == TriggerJson.Type.NO_REASON }) {
            triggers.add(TriggerEntry(id = -1L, type = TriggerJson.Type.NO_REASON, value = 0, percent = 0.0))
        }

        val totalWithKnownTrigger = smokedHistory.count {
            it.trigger != TriggerJson.Type.NO_REASON
        }

        val updatedTriggers = triggers.map { trigger ->
            val count = smokedHistory.count {
                it.trigger == trigger.type
            }

            trigger.copy(
                value = count,
                percent = if (trigger.type == TriggerJson.Type.NO_REASON) 0.0
                else calculatePercentage(count, totalWithKnownTrigger)
            )
        }

        val mostCommon = updatedTriggers.filter { it.value > 0 && it.type != TriggerJson.Type.NO_REASON }.maxByOrNull { it.value }
        return TriggerState(mostCommon = mostCommon, triggers = updatedTriggers)
    }

    private fun calculatePercentage(count: Int, total: Int): Double {
        return if (total > 0) count.toDouble() / total * 100 else 0.0
    }
}