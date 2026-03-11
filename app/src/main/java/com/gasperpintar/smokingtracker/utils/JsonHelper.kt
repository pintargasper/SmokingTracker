package com.gasperpintar.smokingtracker.utils

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.model.AchievementEntry
import com.gasperpintar.smokingtracker.model.AchievementJsonEntry
import com.gasperpintar.smokingtracker.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.type.AchievementUnit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class JsonHelper(
    private val achievementRepository: AchievementRepository
) {
    suspend fun initializeAchievementsIfNeeded(context: Context) {
        val existing = achievementRepository.getAll().associateBy {
            it.id
        }

        if (existing.isNotEmpty()) {
            migrate(achievementRepository)
            return
        }

        val achievements = listOf(
            AchievementCategory.SMOKE_FREE_TIME,
            AchievementCategory.CIGARETTES_AVOIDED
        ).flatMap {
            loadAchievementsFromJson(context, type = it)
        }.map { entry ->
            entry.toEntity(existing[entry.id])
        }
        achievementRepository.upsertAll(entries = achievements)
    }

    fun loadAchievementsFromJson(
        context: Context, type:
        AchievementCategory
    ): List<AchievementEntry> {
        val inputStream = context.resources.openRawResource(R.raw.achievements)
        val reader = InputStreamReader(inputStream)
        val gson = Gson()

        val typeToken = object : TypeToken<Map<String, List<AchievementJsonEntry>>>() {}.type
        val jsonMap: Map<String, List<AchievementJsonEntry>> = gson.fromJson(reader, typeToken)

        val entries = jsonMap[type.name] ?: emptyList()

        return entries.map { jsonEntry ->
            val iconEnum = AchievementIcon.valueOf(value = jsonEntry.icon.uppercase())
            val titleEnum = AchievementTitle.valueOf(value = jsonEntry.title.uppercase())
            val messageEnum = AchievementMessage.valueOf(value = jsonEntry.message.uppercase())
            val unit = if (type == AchievementCategory.CIGARETTES_AVOIDED) {
                AchievementUnit.CIGARETTES
            } else {
                AchievementUnit.valueOf(jsonEntry.unit)
            }
            AchievementEntry(
                id = 0,
                image = iconEnum.name,
                value = jsonEntry.value,
                title = titleEnum.name,
                message = messageEnum.name,
                times = 0L,
                lastAchieved = null,
                reset = true,
                notify = true,
                category = type,
                unit = unit
            )
        }
    }

    suspend fun migrate(achievementRepository: AchievementRepository) {
        val achievements = achievementRepository.getAll()

        achievementRepository.deleteAll()

        val iconEntries = AchievementIcon.entries.toTypedArray()
        val titleEntries = AchievementTitle.entries.toTypedArray()
        val messageEntries = AchievementMessage.entries.toTypedArray()

        val achievementsList = mutableListOf<AchievementEntity>()
        achievements.forEachIndexed { index, achievement ->
            val newImage = iconEntries.getOrNull(index % iconEntries.size)?.name ?: AchievementIcon.entries.first().name
            val newTitle = titleEntries.getOrNull(index % titleEntries.size)?.name ?: AchievementTitle.entries.first().name
            val newMessage = messageEntries.getOrNull(index % messageEntries.size)?.name ?: AchievementMessage.entries.first().name
            val updated = achievement.copy(
                image = newImage,
                title = newTitle,
                message = newMessage,
                id = achievement.id
            )
            achievementsList.add(updated)
        }
        achievementRepository.insert(entries = achievementsList)
    }
}