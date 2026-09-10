package com.gasperpintar.smokingtracker.utils

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.model.AchievementJsonEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonHelper(
    private val achievementRepository: AchievementRepository
) {

    suspend fun initializeAchievements(context: Context) {
        val existing = achievementRepository.getAll()

        val type = object : TypeToken<Map<AchievementCategory, List<AchievementJsonEntry>>>() {}.type
        val rawData: Map<AchievementCategory, List<AchievementJsonEntry>> = context.resources
            .openRawResource(R.raw.achievements).bufferedReader().use { Gson().fromJson(it, type) }

        val newEntries = rawData.flatMap { (category, entries) ->
            entries.filter { entry -> existing.none { it.category == category && it.title == entry.title } }.map { entry ->
                AchievementEntity(
                    id = 0,
                    image = entry.icon.uppercase(),
                    value = entry.value,
                    title = entry.title.uppercase(),
                    message = entry.message.uppercase(),
                    times = 0,
                    lastAchieved = null,
                    reset = true,
                    notify = true,
                    category = category,
                    unit = if (category == AchievementCategory.CIGARETTES_AVOIDED) AchievementUnit.CIGARETTES else enumValueOf(name = entry.unit)
                )
            }
        }

        if (newEntries.isNotEmpty()) {
            achievementRepository.upsertAll(newEntries)
        }
    }
}