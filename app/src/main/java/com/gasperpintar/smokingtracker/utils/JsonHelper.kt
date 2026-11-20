package com.gasperpintar.smokingtracker.utils

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.model.AboutJson
import com.gasperpintar.smokingtracker.model.AchievementJson
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.type.*
import com.gasperpintar.smokingtracker.ui.adapter.ItemAdapter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class JsonHelper(
    private val achievementRepository: AchievementRepository? = null
) {
    suspend fun initializeAchievements(
        context: Context
    ) {
        val existing = achievementRepository?.getAll()

        val rawData: Map<Achievement.Category, List<AchievementJson>> = context.resources
            .openRawResource(R.raw.achievements).bufferedReader().use {
                Gson().fromJson(it, object : TypeToken<Map<Achievement.Category, List<AchievementJson>>>() {}.type)
            }

        val newEntries = rawData.flatMap { (category, entries) ->
            entries.filter { entry -> existing?.none { it.category == category && it.title == entry.title }
                ?: return@flatMap emptyList() }.map { entry ->
                AchievementEntry(
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
                    unit = if (category == Achievement.Category.CIGARETTES_AVOIDED) Achievement.Unit.CIGARETTES else enumValueOf(name = entry.unit)
                )
            }
        }

        if (newEntries.isNotEmpty()) {
            achievementRepository?.upsertAll(newEntries)
        }
    }

    fun initializeAbout(context: Context): Pair<List<ItemAdapter.Item>, List<ItemAdapter.Item>> {
        val gson = Gson()
        val rawData: Map<String, JsonElement> = context.resources.openRawResource(R.raw.about)
            .bufferedReader().use {
                gson.fromJson(it, object : TypeToken<Map<String, JsonElement>>() {}.type)
            }

        val type = object : TypeToken<List<AboutJson>>() {}.type
        val platforms: List<AboutJson> = gson.fromJson(rawData["platforms"], type)
        val contributors: List<AboutJson> = gson.fromJson(rawData["contributors"], type)

        val platformItems = platforms.map { platform ->
            ItemAdapter.Item(title = platform.title, value = platform.value.orEmpty(), url = platform.url)
        }

        val contributorItems = contributors.map { contributor ->
            ItemAdapter.Item(
                title = contributor.title,
                value = contributor.contributions.orEmpty().joinToString(separator = ", ") {
                    context.getString(it.stringResource)
                },
                url = contributor.url
            )
        }
        return platformItems to contributorItems
    }
}