package com.gasperpintar.smokingtracker.utils

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.AppDatabase
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.model.AchievementJsonEntry
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class JsonHelper(private val database: AppDatabase) {

    suspend fun initializeAchievementsIfNeeded(context: Context) {
        val achievementDao = database.achievementDao()
        val achievementsInDb = achievementDao.getAllAchievements()
        val achievementsFromJson = loadAchievementsFromJson(context, AchievementCategory.SMOKE_FREE_TIME) +
                loadAchievementsFromJson(context, AchievementCategory.CIGARETTES_AVOIDED)

        val existingSet = achievementsInDb.map {
            Triple(it.value, it.category, it.unit) to it.message
        }.toSet()
        val newAchievements = achievementsFromJson.filter {
            val key = Triple(it.value, it.category, it.unit) to it.message
            key !in existingSet
        }
        val entities = newAchievements.map { Helper.run { it.toAchievementEntity() } }
        if (entities.isNotEmpty()) {
            achievementDao.insertAll(entities)
        }
    }

    fun loadAchievementsFromJson(context: Context, type: AchievementCategory): List<AchievementEntry> {
        val inputStream = context.resources.openRawResource(R.raw.achievements)
        val reader = InputStreamReader(inputStream)
        val gson = Gson()

        val typeToken = object : TypeToken<Map<String, List<AchievementJsonEntry>>>() {}.type
        val jsonMap: Map<String, List<AchievementJsonEntry>> = gson.fromJson(reader, typeToken)

        val entries = jsonMap[type.name] ?: emptyList()

        return entries.map { jsonEntry ->
            val iconEnum = AchievementIcon.valueOf(jsonEntry.icon.uppercase())
            val unit = if (type == AchievementCategory.CIGARETTES_AVOIDED) {
                AchievementUnit.CIGARETTES
            } else {
                AchievementUnit.valueOf(jsonEntry.unit)
            }
            AchievementEntry(
                id = jsonEntry.id,
                image = iconEnum.drawableRes,
                value = jsonEntry.value,
                message = jsonEntry.description,
                unlockedAt = null,
                category = type,
                unit = unit
            )
        }
    }
}