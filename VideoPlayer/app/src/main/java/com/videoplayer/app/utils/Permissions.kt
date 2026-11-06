package com.videoplayer.app.utils

import android.Manifest
import android.os.Build

object Permissions {
    /** Permission required to read local videos on this Android version. */
    val videoRead: String
        get() = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO
                else Manifest.permission.READ_EXTERNAL_STORAGE
}
