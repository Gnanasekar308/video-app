package com.videoplayer.app

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.videoplayer.app.ui.navigation.AppNav
import com.videoplayer.app.ui.theme.VideoPlayerTheme
import com.videoplayer.app.utils.Permissions

// FragmentActivity so BiometricPrompt can attach.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoPlayerTheme {
                val ctx = LocalContext.current
                var granted by remember {
                    mutableStateOf(ContextCompat.checkSelfPermission(ctx, Permissions.videoRead) == android.content.pm.PackageManager.PERMISSION_GRANTED)
                }
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
                LaunchedEffect(Unit) { if (!granted) launcher.launch(Permissions.videoRead) }
                if (granted) AppNav() else Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Button({ launcher.launch(Permissions.videoRead) }) { Text("Allow video access") }
                }
            }
        }
    }
}
