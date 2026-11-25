package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object ViewHelper {

    fun hideKeyboardAndClearFocus(view: View) {
        val root = view.rootView
        val focusedView = root.findFocus() ?: view

        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)

        focusedView.clearFocus()
        root.isFocusableInTouchMode = true
        root.requestFocus()
    }
}