package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale

@RunWith(value = AndroidJUnit4::class)
class FileHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        val baseContext: Context = ApplicationProvider.getApplicationContext()
        val configuration = baseContext.resources.configuration
        configuration.setLocale(Locale.GERMAN)
        context = baseContext.createConfigurationContext(configuration)
    }

    @Test
    fun getFileNameReturnsCorrectFileNameForExcelFile() {
        val temporaryFile = File.createTempFile("test_data", ".xlsx")

        try {
            val expected = temporaryFile.name
            val actual = FileHelper.getFileName(
                context,
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    temporaryFile
                )
            )
            assertEquals(expected, actual)
        } finally {
            temporaryFile.delete()
        }
    }

    @Test
    fun getFileNameReturnsUnknownWhenUriIsNull() {
        val expected = context.getString(R.string.restore_popup_file_unknown)
        val actual = FileHelper.getFileName(context, null)
        assertEquals(expected, actual)
    }
}