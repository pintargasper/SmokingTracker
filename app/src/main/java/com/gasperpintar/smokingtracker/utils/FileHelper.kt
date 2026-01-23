package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.gasperpintar.smokingtracker.R

object FileHelper {

    fun getFileName(context: Context, uri: Uri?): String {
        var name = context.getString(R.string.upload_popup_file_unknown)
        if (uri == null) return name

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
