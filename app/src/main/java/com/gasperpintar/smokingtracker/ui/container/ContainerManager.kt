package com.gasperpintar.smokingtracker.ui.container

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.database.model.AchievementEntry
import com.gasperpintar.smokingtracker.database.model.CostEntry
import com.gasperpintar.smokingtracker.database.model.HistoryEntry
import com.gasperpintar.smokingtracker.database.model.NoteEntry
import com.gasperpintar.smokingtracker.databinding.ContainerAchievementsBinding
import com.gasperpintar.smokingtracker.databinding.ContainerContentCostBinding
import com.gasperpintar.smokingtracker.databinding.ContainerContentHistoryBinding
import com.gasperpintar.smokingtracker.databinding.ContainerContentNoteBinding
import com.gasperpintar.smokingtracker.type.AchievementIcon
import com.gasperpintar.smokingtracker.type.AchievementMessage
import com.gasperpintar.smokingtracker.type.AchievementTitle
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatDate
import java.text.DecimalFormat

object ContainerManager {

    fun createHistoryAdapter(
        context: FragmentActivity,
        onEdit: (HistoryEntry) -> Unit,
        onDelete: (HistoryEntry) -> Unit
    ) = BaseContainer.create<HistoryEntry, ContainerContentHistoryBinding>(
        context = context,
        inflate = ContainerContentHistoryBinding::inflate) { binding, entry ->

        binding.timerLabel.text = entry.timerLabel
        binding.lent.isVisible = entry.isLent

        binding.edit.setOnClickListener {
            onEdit(entry)
        }

        binding.delete.setOnClickListener {
            onDelete(entry)
        }
    }

    fun createCostAdapter(
        context: FragmentActivity,
        currency: String,
        onSuccess: (CostEntry) -> Unit
    ) = BaseContainer.create<CostEntry, ContainerContentCostBinding>(
        context = context,
        inflate = ContainerContentCostBinding::inflate) { binding, costEntry ->

        binding.dateLabel.text = context.getString(
            R.string.cost_format,
            formatDate(context = context, date = costEntry.startDate.toLocalDate()),
            formatDate(context = context, date = costEntry.endDate.toLocalDate())
        )

        binding.priceLabel.text = context.getString(
            R.string.cost_price,
            DecimalFormat("0.00#").format(costEntry.price),
            currency
        )

        binding.delete.setOnClickListener {
            onSuccess(costEntry)
        }
    }

    fun createAchievementAdapter(
        context: FragmentActivity
    ) = BaseContainer.create<AchievementEntry, ContainerAchievementsBinding>(
        context = context,
        inflate = ContainerAchievementsBinding::inflate) { binding, achievementEntry ->

        binding.root.isClickable = false
        binding.root.isFocusable = false

        binding.achievementTitle.text = context.getString(AchievementTitle.valueOf(achievementEntry.title).stringResource)
        binding.achievementMessage.text = context.getString(AchievementMessage.valueOf(achievementEntry.message).stringResource)

        binding.lastAchieved.text =
            achievementEntry.lastAchieved
                ?.toLocalDate()
                ?.let { localDate ->
                    context.getString(R.string.achievement_last, formatDate(localDate))
                } ?: context.getString(R.string.achievement_last, "/")

        val achievedTimesText = context.resources.getQuantityString(
            R.plurals.achievement_achieved_times,
            achievementEntry.times.toInt(),
            achievementEntry.times
        )

        binding.achievedCount.text = context.getString(R.string.achievement_achieved, achievedTimesText)
        binding.imageAchievement.setImageResource(
            AchievementIcon.valueOf(achievementEntry.image).drawableResource
        )

        when (achievementEntry.times) {
            0L -> {
                binding.imageAchievement.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                    setSaturation(0f)
                })
                binding.imageAchievement.alpha = 0.4f
            }
            else -> {
                binding.imageAchievement.clearColorFilter()
                binding.imageAchievement.alpha = 1f
            }
        }
    }

    fun createNoteAdapter(
        context: FragmentActivity,
        onOpen: (NoteEntry) -> Unit,
        onDelete: (NoteEntry) -> Unit
    ) = BaseContainer.create<NoteEntry, ContainerContentNoteBinding>(
        context = context,
        inflate = ContainerContentNoteBinding::inflate) { binding, noteEntry ->

        binding.emotionIcon.setImageResource(noteEntry.moodIcon)
        binding.titleLabel.text = noteEntry.title
        binding.contentLabel.text = noteEntry.content

        binding.root.setOnClickListener {
            onOpen(noteEntry)
        }

        binding.delete.setOnClickListener {
            onDelete(noteEntry)
        }
    }
}