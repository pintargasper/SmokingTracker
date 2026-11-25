package com.gasperpintar.smokingtracker.model

import com.gasperpintar.smokingtracker.R
import com.google.gson.annotations.SerializedName

data class TriggerJson(

    @SerializedName(value = "type")
    val type: Type,
) {

    enum class Type {
        @SerializedName(value = "STRESS")
        STRESS,

        @SerializedName(value = "COFFEE")
        COFFEE,

        @SerializedName(value = "ALCOHOL")
        ALCOHOL,

        @SerializedName(value = "BOREDOM")
        BOREDOM,

        @SerializedName(value = "SOCIAL")
        SOCIAL,

        @SerializedName(value = "AFTER_MEAL")
        AFTER_MEAL,

        @SerializedName(value = "HABIT")
        HABIT,

        @SerializedName(value = "NO_REASON")
        NO_REASON
    }

    enum class Title(
        val stringResource: Int
    ) {
        STRESS(R.string.trigger_stress),
        COFFEE(R.string.trigger_coffee),
        ALCOHOL(R.string.trigger_alcohol),
        BOREDOM(R.string.trigger_boredom),
        SOCIAL(R.string.trigger_social),
        AFTER_MEAL(R.string.trigger_after_meal),
        HABIT(R.string.trigger_habit),
        NO_REASON(R.string.trigger_no_reason)
    }

    enum class Icon(
        val drawableResource: Int
    ) {
        STRESS(R.drawable.psychology_48px),
        COFFEE(R.drawable.coffee_48px),
        ALCOHOL(R.drawable.local_bar_48px),
        BOREDOM(R.drawable.sentiment_worried_48px),
        SOCIAL(R.drawable.groups_48px),
        AFTER_MEAL(R.drawable.restaurant_48px),
        HABIT(R.drawable.repeat_48px),
        NO_REASON(R.drawable.question_mark_48px)
    }
}