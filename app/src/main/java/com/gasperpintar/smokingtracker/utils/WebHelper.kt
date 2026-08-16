package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object WebHelper {

    fun openUrl(
        context: Context,
        url: String
    ) {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}