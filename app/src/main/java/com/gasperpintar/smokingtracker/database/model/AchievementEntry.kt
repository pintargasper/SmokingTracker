package com.gasperpintar.smokingtracker.database.model

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker._interface.IIdentifiable
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.type.Achievement
import java.time.LocalDateTime

data class AchievementEntry(
    @get:Override
    override val id: Long,
    val image: String,
    val value: Int,
    val title: String,
    val message: String,
    val times: Long,
    val lastAchieved: LocalDateTime?,
    val reset: Boolean,
    val notify: Boolean,
    val category: Achievement.Category,
    val unit: Achievement.Unit
): IIdentifiable {
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

    fun toEntity(): AchievementEntity {
        return AchievementEntity(
            id = id,
            image = image,
            value = value,
            title = title,
            message = message,
            times = times,
            lastAchieved = lastAchieved,
            reset = reset,
            notify = notify,
            category = category,
            unit = unit
        )
    }

    fun getDisplayText(
        context: Context
    ): String {
        val resource = when (unit) {
            Achievement.Unit.HOURS -> R.plurals.time_hours
            Achievement.Unit.DAYS -> R.plurals.time_days
            Achievement.Unit.WEEKS -> R.plurals.time_weeks
            Achievement.Unit.MONTHS -> R.plurals.time_months
            Achievement.Unit.YEARS -> R.plurals.time_years
            Achievement.Unit.CIGARETTES -> R.plurals.cigarettes_count
        }
        return context.resources.getQuantityString(resource, value, value)
    }
}