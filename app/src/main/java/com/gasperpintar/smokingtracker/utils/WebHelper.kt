package com.gasperpintar.smokingtracker.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object WebHelper {

    fun Context.openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}