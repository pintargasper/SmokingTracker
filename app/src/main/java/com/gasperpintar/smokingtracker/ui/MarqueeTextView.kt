package com.gasperpintar.smokingtracker.ui

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView : AppCompatTextView {

    constructor(
        context: Context
    ) : super(context) {
        initializeMarquee()
    }

    constructor(
        context: Context, attributeSet: AttributeSet
    ) : super(context, attributeSet) {
        initializeMarquee()
    }

    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defaultStyleAttribute: Int
    ) : super(
        context,
        attributeSet,
        defaultStyleAttribute) {
        initializeMarquee()
    }

    override fun isFocused(): Boolean {
        return true
    }

    private fun initializeMarquee() {
        this.isSingleLine = true
        this.ellipsize = TextUtils.TruncateAt.MARQUEE
        this.marqueeRepeatLimit = -1
        this.isFocusable = false
        this.isFocusableInTouchMode = false
        this.setHorizontallyScrolling(true)
        this.isSelected = true
    }
}
