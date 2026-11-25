package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class ViewHelperTest {

    private lateinit var context: Context
    private lateinit var root: LinearLayout
    private lateinit var editText: EditText

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        root = LinearLayout(context).apply {
            isFocusableInTouchMode = true
        }

        editText = EditText(context)

        root.addView(
            editText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.requestFocus()
        editText.requestFocus()
    }

    @Test
    fun hideKeyboardAndClearFocusClearsEditTextFocus() {
        assertEquals(editText, root.findFocus())

        ViewHelper.hideKeyboardAndClearFocus(view = root)

        assertEquals(root, root.findFocus())
    }

    @Test
    fun hideKeyboardAndClearFocusDoesNotCrashWithoutFocusedChild() {
        root.requestFocus()

        assertNotNull(root.findFocus())

        assertEquals(null, runCatching {
            ViewHelper.hideKeyboardAndClearFocus(view = root)
        }.exceptionOrNull())
    }
}