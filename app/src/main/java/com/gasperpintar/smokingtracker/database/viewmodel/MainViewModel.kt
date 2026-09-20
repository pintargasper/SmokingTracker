package com.gasperpintar.smokingtracker.database.viewmodel

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.NotificationsSettingsEntity
import com.gasperpintar.smokingtracker.database.entity.SettingsEntity
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.CostsRepository
import com.gasperpintar.smokingtracker.database.repository.NotificationsSettingsRepository
import com.gasperpintar.smokingtracker.database.repository.SettingsRepository
import com.gasperpintar.smokingtracker.database.viewmodel.state.MainState
import com.gasperpintar.smokingtracker.utils.JsonHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class MainViewModel(
    private val achievementRepository: AchievementRepository,
    private val costsRepository: CostsRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationsSettingsRepository: NotificationsSettingsRepository
): ViewModel() {

    init {
        viewModelScope.launch {
            updateLastCostPeriod()
        }
    }

    suspend fun getState(
        context: Context
    ): MainState {
        notificationsSettingsRepository.get() ?: NotificationsSettingsEntity.default().also {
            notificationsSettingsRepository.insert(settings = it)
        }

        settingsRepository.get() ?: SettingsEntity.default(language = getLanguage(context = context)).also {
            settingsRepository.insert(settings = it)
        }

        return MainState(
            isNewVersion = handleAppVersioning(context = context)
        )
    }

    private suspend fun handleAppVersioning(
        context: Context
    ): Boolean {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val lastVersionName = sharedPreferences.getString("last_version_name", null)

        val isNewVersion = versionName != lastVersionName
        if (isNewVersion) {
            JsonHelper(achievementRepository = achievementRepository).initializeAchievements(context = context)
            sharedPreferences.edit { putString("last_version_name", versionName) }
        }
        return isNewVersion
    }

    private suspend fun updateLastCostPeriod() {
        val lastEntry = costsRepository.getLast() ?: return
        val today = LocalDate.now()
        val lastEndDate = lastEntry.endDate.toLocalDate()

        if (lastEndDate.isEqual(today.minusDays(1))) {
            costsRepository.update(entry = lastEntry.copy(endDate = today.atTime(LocalTime.MAX)))
        }
    }

    private fun getLanguage(
        context: Context
    ): String {
        val languageValues = context.resources.getStringArray(R.array.language_values)
        return languageValues.firstOrNull { it == Locale.getDefault().toLanguageTag() } ?: "system"
    }
}