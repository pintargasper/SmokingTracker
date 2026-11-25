package com.gasperpintar.smokingtracker.ui.container

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
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
import com.gasperpintar.smokingtracker.databinding.ContainerContentHistoryOptionsBinding
import com.gasperpintar.smokingtracker.databinding.ContainerContentNoteBinding
import com.gasperpintar.smokingtracker.model.AchievementJson
import com.gasperpintar.smokingtracker.model.TriggerJson
import com.gasperpintar.smokingtracker.utils.LocalizationHelper.formatDate
import com.gasperpintar.smokingtracker.utils.TimeHelper
import java.text.DecimalFormat
import java.time.Duration

object ContainerManager {

    fun createHistoryAdapter(
        context: FragmentActivity,
        entries: () -> List<HistoryEntry>,
        showIntervalLabels: () -> Boolean,
        onEdit: (HistoryEntry) -> Unit,
        onDelete: (HistoryEntry) -> Unit
    ) = BaseContainer.create<HistoryEntry, ContainerContentHistoryBinding>(context = context, inflate = ContainerContentHistoryBinding::inflate) { binding, entry ->
        binding.timerLabel.text = entry.timerLabel
        binding.lent.isVisible = entry.isLent

        binding.triggerIcon.apply {
            isVisible = entry.trigger != TriggerJson.Type.NO_REASON
            if (isVisible) setImageResource(TriggerJson.Icon.valueOf(entry.trigger.name).drawableResource)
        }

        val previousEntry = entries().asSequence().filter {
            it.createdAt.toLocalDate() == entry.createdAt.toLocalDate() && it.createdAt.isBefore(entry.createdAt)
        }.maxByOrNull { it.createdAt }

        binding.intervalLabel.apply {
            isVisible = showIntervalLabels()
            text = previousEntry?.let {
                TimeHelper.formatTime(
                    resources = context.resources,
                    totalMinutes = Duration.between(it.createdAt, entry.createdAt).toMinutes().toInt()
                )
            } ?: context.getString(R.string.home_first_today)
        }

        binding.more.setOnClickListener { view ->
            val optionsBinding = ContainerContentHistoryOptionsBinding.inflate(LayoutInflater.from(context), binding.root, false)
            optionsBinding.root.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )

            PopupWindow(optionsBinding.root, optionsBinding.root.measuredWidth, optionsBinding.root.measuredHeight, true).apply {
                optionsBinding.historyEdit.setOnClickListener {
                    onEdit(entry)
                    dismiss()
                }
                optionsBinding.historyDelete.setOnClickListener {
                    onDelete(entry)
                    dismiss()
                }
                showAsDropDown(view, 0, 0, Gravity.END)
            }
        }
    }

    fun createCostAdapter(
        context: FragmentActivity,
        currency: String,
        onSuccess: (CostEntry) -> Unit
    ) = BaseContainer.create<CostEntry, ContainerContentCostBinding>(context = context, inflate = ContainerContentCostBinding::inflate
    ) { binding, costEntry ->
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
    ) = BaseContainer.create<AchievementEntry, ContainerAchievementsBinding>(context = context, inflate = ContainerAchievementsBinding::inflate) { binding, achievementEntry ->
        binding.root.isClickable = false
        binding.root.isFocusable = false

        binding.achievementTitle.text = context.getString(AchievementJson.Title.valueOf(achievementEntry.title).stringResource)
        binding.achievementMessage.text = context.getString(AchievementJson.Message.valueOf(achievementEntry.message).stringResource)

        binding.lastAchieved.text = achievementEntry.lastAchieved?.toLocalDate()?.let { localDate ->
            context.getString(R.string.achievement_last, formatDate(localDate))
        } ?: context.getString(R.string.achievement_last, "/")

        val achievedTimesText = context.resources.getQuantityString(
            R.plurals.achievement_achieved_times,
            achievementEntry.times.toInt(),
            achievementEntry.times
        )

        binding.achievedCount.text = context.getString(R.string.achievement_achieved, achievedTimesText)
        binding.imageAchievement.setImageResource(
            AchievementJson.Icon.valueOf(achievementEntry.image).drawableResource
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
    ) = BaseContainer.create<NoteEntry, ContainerContentNoteBinding>(context = context, inflate = ContainerContentNoteBinding::inflate) { binding, noteEntry ->
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