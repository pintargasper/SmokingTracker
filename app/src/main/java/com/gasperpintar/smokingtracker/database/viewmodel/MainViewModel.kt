package com.gasperpintar.smokingtracker.database.viewmodel

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.viewmodel.state.MainUiState
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.repository.CostsRepository
import com.gasperpintar.smokingtracker.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.repository.SettingsRepository
import com.gasperpintar.smokingtracker.utils.JsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class MainViewModel(
    application: Application,
    private val achievementRepository: AchievementRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationsSettingsRepository: NotificationsSettingsRepository,
    private val costsRepository: CostsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        load(context = application.applicationContext)
    }

    private fun load(context: Context) {
        viewModelScope.launch {
            val settings = getOrCreateSettings(context = context)

            handleAppVersioning(context)
            updateLastCostPeriod()

            _uiState.value = _uiState.value.copy(settings = settings)
        }
    }

    private suspend fun getOrCreateSettings(context: Context): SettingsEntity {
        val settings = settingsRepository.get() ?: SettingsEntity.default(language = getLanguageIndex(context = context)).also {
            settingsRepository.insert(settings = it)
        }

        notificationsSettingsRepository.get() ?: NotificationsSettingsEntity.default().also {
            notificationsSettingsRepository.insert(settings = it)
        }
        return settings
    }

    private suspend fun handleAppVersioning(context: Context) {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val lastVersionName = sharedPreferences.getString("last_version_name", null)

        if (versionName != lastVersionName) {
            JsonHelper(achievementRepository = achievementRepository).initializeAchievementsIfNeeded(context = context)
            sharedPreferences.edit { putString("last_version_name", versionName) }
        }
    }

    private suspend fun updateLastCostPeriod() {
        val lastEntry = costsRepository.getLast() ?: return
        val today = LocalDate.now()
        val lastEndDate = lastEntry.endDate.toLocalDate()

        if (lastEndDate.isEqual(today.minusDays(1))) {
            costsRepository.update(entry = lastEntry.copy(endDate = today.atTime(LocalTime.MAX)))
        }
    }

    private fun getLanguageIndex(context: Context): Int {
        val languageValues = context.resources.getStringArray(R.array.language_values)
        return languageValues.indexOf(Locale.getDefault().toLanguageTag()).takeIf { it >= 0 } ?: 0
    }
}