package com.gasperpintar.smokingtracker.database.viewmodel

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.repository.TriggerRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.TriggerState

class TriggerViewModel(
    private val triggerRepository: TriggerRepository
) : ViewModel() {

    suspend fun getState(): TriggerState {
        return TriggerState(triggers = triggerRepository.getAll())
    }
}