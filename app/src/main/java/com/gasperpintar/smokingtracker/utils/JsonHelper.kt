package com.gasperpintar.smokingtracker.utils

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.model.TriggerEntry
import com.gasperpintar.smokingtracker.model.AboutJson
import com.gasperpintar.smokingtracker.model.AchievementJson
import com.gasperpintar.smokingtracker.database.repository.AchievementRepository
import com.gasperpintar.smokingtracker.database.repository.TriggerRepository
import com.gasperpintar.smokingtracker.model.TriggerJson
import com.gasperpintar.smokingtracker.ui.adapter.ItemAdapter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.util.Locale.getDefault

class JsonHelper(
    private val achievementRepository: AchievementRepository? = null,
    private val triggerRepository: TriggerRepository? = null
) {
    suspend fun initializeAchievements(
        context: Context
    ) {
        val existing = achievementRepository?.getAll()
        val rawData: Map<AchievementJson.Category, List<AchievementJson>> = context.resources
            .openRawResource(R.raw.achievements).bufferedReader().use {
                Gson().fromJson(it, object : TypeToken<Map<AchievementJson.Category, List<AchievementJson>>>() {}.type)
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
                    unit = if (category == AchievementJson.Category.CIGARETTES_AVOIDED) AchievementJson.Unit.CIGARETTES else enumValueOf(name = entry.unit)
                )
            }
        }

        if (newEntries.isNotEmpty()) {
            achievementRepository?.upsertAll(newEntries)
        }
    }

    suspend fun initializeTriggers(context: Context) {
        val existing = triggerRepository?.getAll()

        val rawData: Map<String, JsonElement> = context.resources
            .openRawResource(R.raw.triggers).bufferedReader().use {
                Gson().fromJson(it, object : TypeToken<Map<String, JsonElement>>() {}.type)
            }

        val type = object : TypeToken<List<TriggerJson>>() {}.type
        val triggers: List<TriggerJson> = Gson().fromJson(rawData["triggers"], type)

        val newEntries = triggers.filter { entry -> existing?.none { it.type == entry.type } ?: return }.map { entry ->
            TriggerEntry(id = 0, type = entry.type)
        }

        if (newEntries.isNotEmpty()) {
            triggerRepository?.upsertAll(newEntries)
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
                    context.getString(it.stringResource).lowercase(locale = getDefault())
                },
                url = contributor.url
            )
        }
        return platformItems to contributorItems
    }
}