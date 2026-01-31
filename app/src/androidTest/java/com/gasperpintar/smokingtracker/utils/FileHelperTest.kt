package com.gasperpintar.smokingtracker.utils

import android.content.Context
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import com.gasperpintar.smokingtracker.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Locale

class FileHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        val baseContext: Context = ApplicationProvider.getApplicationContext()
        val configuration = baseContext.resources.configuration
        configuration.setLocale(Locale.ENGLISH)

        context = baseContext.createConfigurationContext(configuration)
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

        val result = FileHelper.getFileName(context, uri)
        assert(result.isNotEmpty())
        temporaryFile.delete()

        val resultNull = FileHelper.getFileName(context, null)
        assertEquals(context.getString(R.string.upload_popup_file_unknown), resultNull)
    }
}