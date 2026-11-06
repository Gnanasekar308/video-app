package com.videoplayer.app.ui.player

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.videoplayer.app.player.PlayerManager
import com.videoplayer.app.ui.theme.VideoPlayerTheme

/** Hosts the player. Supports rotation, fullscreen immersive mode, local files and vault playback. */
class PlayerActivity : ComponentActivity() {
    private lateinit var manager: PlayerManager
    private var isVault = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isVault = intent.getBooleanExtra(EXTRA_VAULT, false)
        if (isVault) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // block screenshots in vault
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val uris = intent.getStringArrayListExtra(EXTRA_URIS)?.map { it.toUri() }
            ?: listOfNotNull(intent.data)
        val ids = intent.getLongArrayExtra(EXTRA_IDS)?.toList() ?: emptyList()
        val index = intent.getIntExtra(EXTRA_INDEX, 0)

        manager = PlayerManager(this)
        setContent {
            VideoPlayerTheme {
                PlayerScreen(manager, this, uris, ids, index, isVault, onBack = { finish() })
            }
        }
    }

    fun setFullscreen(enabled: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, !enabled)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (enabled) hide(WindowInsetsCompat.Type.systemBars()) else show(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun toggleOrientation() {
        requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onStop() { super.onStop(); manager.exo.pause() }
    override fun onDestroy() { manager.release(); super.onDestroy() }

    companion object {
        const val EXTRA_URIS = "uris"
        const val EXTRA_IDS = "ids"
        const val EXTRA_INDEX = "index"
        const val EXTRA_VAULT = "vault"
    }
}
