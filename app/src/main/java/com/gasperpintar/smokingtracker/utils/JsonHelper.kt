package com.gasperpintar.smokingtracker.utils

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.database.model.AboutEntry
import com.gasperpintar.smokingtracker.database.model.AchievementJsonEntry
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.*
import com.gasperpintar.smokingtracker.ui.adapter.PageAdapter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class JsonHelper(
    private val achievementRepository: AchievementRepository? = null
) {
    suspend fun initializeAchievements(context: Context) {
        val existing = achievementRepository?.getAll()

        val rawData: Map<AchievementCategory, List<AchievementJsonEntry>> = context.resources
            .openRawResource(R.raw.achievements).bufferedReader().use {
                Gson().fromJson(it, object : TypeToken<Map<AchievementCategory, List<AchievementJsonEntry>>>() {}.type)
            }

        val newEntries = rawData.flatMap { (category, entries) ->
            entries.filter { entry -> existing?.none { it.category == category && it.title == entry.title }
                ?: return@flatMap emptyList() }.map { entry ->
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
            achievementRepository?.upsertAll(newEntries)
        }
    }

    fun initializeAbout(context: Context): Pair<List<PageAdapter.Page>, List<PageAdapter.Page>> {
        val gson = Gson()
        val rawData: Map<String, JsonElement> = context.resources.openRawResource(R.raw.about)
            .bufferedReader().use {
                gson.fromJson(it, object : TypeToken<Map<String, JsonElement>>() {}.type)
            }

        val type = object : TypeToken<List<AboutEntry>>() {}.type
        val platforms: List<AboutEntry> = gson.fromJson(rawData["platforms"], type)
        val contributors: List<AboutEntry> = gson.fromJson(rawData["contributors"], type)

        val platformPages = platforms.map { platform ->
            PageAdapter.Page(title = platform.title, value = platform.value.orEmpty(), url = platform.url)
        }

        val contributorPages = contributors.map { contributor ->
            PageAdapter.Page(
                title = contributor.title,
                value = contributor.contributions.orEmpty().joinToString(separator = ", ") {
                    context.getString(it.stringResource)
                },
                url = contributor.url
            )
        }
        return platformPages to contributorPages
    }
}