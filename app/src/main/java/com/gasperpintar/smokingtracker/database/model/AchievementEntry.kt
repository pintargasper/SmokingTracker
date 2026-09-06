package com.gasperpintar.smokingtracker.database.model

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import java.time.LocalDateTime

data class AchievementEntry(
    override val id: Long,
    val image: String,
    val value: Int,
    val title: String,
    val message: String,
    val times: Long,
    val lastAchieved: LocalDateTime?,
    val reset: Boolean,
    val notify: Boolean,
    val category: AchievementCategory,
    val unit: AchievementUnit
): Identifiable {
    companion object {
        fun fromEntity(
            entity: AchievementEntity
        ): AchievementEntry {
            return AchievementEntry(
                id = entity.id,
                image = entity.image,
                value = entity.value,
                title = entity.title,
                message = entity.message,
                times = entity.times,
                lastAchieved = entity.lastAchieved,
                reset = entity.reset,
                notify = entity.notify,
                category = entity.category,
                unit = entity.unit
            )
        }
    }

    fun toEntity(existing: AchievementEntity? = null): AchievementEntity {
        return AchievementEntity(
            id = id,
            image = image,
            value = value,
            title = title,
            message = message,
            category = category,
            unit = unit,
            times = existing?.times ?: times,
            lastAchieved = existing?.lastAchieved ?: lastAchieved,
            reset = existing?.reset ?: reset,
            notify = existing?.notify ?: notify
        )
    }

    fun getDisplayText(
        context: Context
    ): String {
        val resource = when (unit) {
            AchievementUnit.HOURS -> R.plurals.time_hours
            AchievementUnit.DAYS -> R.plurals.time_days
            AchievementUnit.WEEKS -> R.plurals.time_weeks
            AchievementUnit.MONTHS -> R.plurals.time_months
            AchievementUnit.YEARS -> R.plurals.time_years
            AchievementUnit.CIGARETTES -> R.plurals.cigarettes_count
        }
        return context.resources.getQuantityString(
            resource,
            value,
            value
        )
    }
}