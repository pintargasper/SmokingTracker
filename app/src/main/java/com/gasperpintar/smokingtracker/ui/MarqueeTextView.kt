package com.gasperpintar.smokingtracker.ui

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        isFocusable = false
        isFocusableInTouchMode = false
        isSelected = true
    }

    @Override
    override fun isFocused(): Boolean = true
}