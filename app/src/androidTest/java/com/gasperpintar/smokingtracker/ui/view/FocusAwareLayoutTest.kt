package com.gasperpintar.smokingtracker.ui.view

import android.content.Context
import android.view.MotionEvent
import android.view.View
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
class FocusAwareLayoutTest {

    private lateinit var context: Context
    private lateinit var layout: FocusAwareLayout
    private lateinit var editText: EditText

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        layout = FocusAwareLayout(context).apply {
            isFocusableInTouchMode = true
        }

        editText = EditText(context)
        layout.addView(editText, LinearLayout.LayoutParams(200, 100))
        layout.measure(
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY)
        )
        layout.layout(0, 0, 300, 300)
        layout.requestFocus()
        editText.requestFocus()
    }

    @Test
    fun touchOutsideEditTextClearsFocus() {
        assertEquals(editText, layout.findFocus())

        dispatchTouch(x = 250f, y = 150f)

        assertEquals(layout, layout.findFocus())
    }

    @Test
    fun touchInsideEditTextKeepsFocus() {
        assertEquals(editText, layout.findFocus())

        dispatchTouch(x = 100f, y = 50f)

        assertEquals(editText, layout.findFocus())
    }

    @Test
    fun touchOutsideWithoutFocusedEditTextDoesNotCrash() {
        layout.requestFocus()

        assertNotNull(layout.findFocus())

        assertEquals(null, runCatching {
            dispatchTouch(x = 250f, y = 150f)
        }.exceptionOrNull())
    }

    private fun dispatchTouch(x: Float, y: Float) {
        val millis = System.currentTimeMillis()
        val event = MotionEvent.obtain(
            millis,
            millis,
            MotionEvent.ACTION_DOWN, x, y, 0
        )

        try {
            layout.dispatchTouchEvent(event)
        } finally {
            event.recycle()
        }
    }
}