package com.gasperpintar.smokingtracker.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout

class FocusAwareLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    @Override
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) clearFocusIfTouchedOutside(event)
        return super.dispatchTouchEvent(event)
    }

    private fun clearFocusIfTouchedOutside(event: MotionEvent) {
        val focusedView = findFocus() as? EditText ?: return
        val location = IntArray(size = 2)

        focusedView.getLocationOnScreen(location)
        val isOutside = event.rawX !in location[0].toFloat()..(location[0] + focusedView.width).toFloat() ||
                event.rawY !in location[1].toFloat()..(location[1] + focusedView.height).toFloat()

        if (!isOutside) return
        hideKeyboard(focusedView)
        focusedView.clearFocus()
        requestFocus()
    }

    private fun hideKeyboard(view: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}