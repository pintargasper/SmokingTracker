package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasperpintar.smokingtracker.R
import com.gasperpintar.smokingtracker.utils.Helper.getDayOfWeekName
import com.gasperpintar.smokingtracker.utils.Helper.getFileName
import com.gasperpintar.smokingtracker.utils.Helper.getMonthName
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.DayOfWeek
import java.time.Month
import java.util.Locale

@RunWith(value = AndroidJUnit4::class)
class HelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        val baseContext: Context = ApplicationProvider.getApplicationContext()
        val configuration = baseContext.resources.configuration
        configuration.setLocale(Locale.ENGLISH)

        context = baseContext.createConfigurationContext(configuration)
    }

    @Test
    fun getDayOfWeekNameReturnsNonEmptyString() {
        for(day in DayOfWeek.entries) {
            val result = context.getDayOfWeekName(dayOfWeek = day)
            assert(result.isNotEmpty())
        }
    }

    @Test
    fun getMonthNameReturnsNonEmptyString() {
        for(month in Month.entries) {
            val result = context.getMonthName(month = month)
            assert(result.isNotEmpty())
        }
    }

    @Test
    fun getFileNameReturnsCorrectNameForExcelFileAndUnknownForNullUri() {
        val temporaryFile = File.createTempFile("test_data", ".xlsx")
        temporaryFile.writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04))
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            temporaryFile
        )

        val result = getFileName(context, uri)
        assert(result.isNotEmpty())
        temporaryFile.delete()

        val resultNull = getFileName(context, null)
        assertEquals(context.getString(R.string.upload_popup_file_unknown), resultNull)
    }
}