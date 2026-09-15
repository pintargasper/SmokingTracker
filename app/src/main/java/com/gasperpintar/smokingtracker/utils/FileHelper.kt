package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.gasperpintar.smokingtracker.R

object FileHelper {

    fun getFileName(
        context: Context,
        uri: Uri?
    ): String {
        val name = context.getString(R.string.restore_popup_file_unknown)
        if (uri == null) return name

        return when (uri.scheme) {
            "file" -> uri.lastPathSegment ?: name
            else -> context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else null
            } ?: name
        }
    }
}