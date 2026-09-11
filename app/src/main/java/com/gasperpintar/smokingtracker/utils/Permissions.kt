package com.gasperpintar.smokingtracker.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

open class Permissions(
    private val activity: ComponentActivity
) {

    private var permissionCallback: ((Boolean) -> Unit)? = null

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionCallback?.invoke(isGranted)
            permissionCallback = null
        }

    val isNotificationPermissionGranted: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    fun checkAndRequestNotificationPermission(callback: (Boolean) -> Unit) {
        if (isNotificationPermissionGranted) return callback(true)
        permissionCallback = callback
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}