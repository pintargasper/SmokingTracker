package com.gasperpintar.smokingtracker.model

import android.content.Context
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker._interface.Identifiable
import com.gasperpintar.smokingtracker.database.entity.AchievementEntity
import com.gasperpintar.smokingtracker.type.AchievementCategory
import com.gasperpintar.smokingtracker.type.AchievementUnit
import java.time.LocalDateTime

data class AchievementEntry(
    override val id: Long,
    val image: Int,
    val value: Int,
    val title: Int,
    val message: Int,
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
        return when (unit) {
            AchievementUnit.HOURS ->
                context.resources.getQuantityString(
                    R.plurals.time_hours,
                    value,
                    value
                )
            AchievementUnit.DAYS ->
                context.resources.getQuantityString(
                    R.plurals.time_days,
                    value,
                    value
                )
            AchievementUnit.WEEKS ->
                context.resources.getQuantityString(
                    R.plurals.time_weeks,
                    value,
                    value
                )
            AchievementUnit.MONTHS ->
                context.resources.getQuantityString(
                    R.plurals.time_months,
                    value,
                    value
                )
            AchievementUnit.YEARS ->
                context.resources.getQuantityString(
                    R.plurals.time_years,
                    value,
                    value
                )
            AchievementUnit.CIGARETTES ->
                context.resources.getQuantityString(
                    R.plurals.cigarettes_count,
                    value,
                    value
                )
        }
    }
}
