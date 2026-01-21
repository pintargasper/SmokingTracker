package com.gasperpintar.smokingtracker.model

import androidx.lifecycle.ViewModel
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.database.entity.HistoryEntity
import com.gasperpintar.smokingtracker.ui.fragment.achievements.AchievementEvaluator
import java.time.LocalDateTime

class HomeViewModel(
    database: AppDatabase
) : ViewModel() {

    private val achievementEvaluator: AchievementEvaluator =
        AchievementEvaluator(achievementDao = database.achievementDao())

    suspend fun onLastEntryChanged(lastEntry: HistoryEntity?) {
        if (lastEntry == null) {
            return
        }

        achievementEvaluator.evaluate(
            lastSmokeTime = lastEntry.createdAt,
            now = LocalDateTime.now()
        )
    }

    fun resetAchievementsCache() {
        achievementEvaluator.reset()
    }
}
