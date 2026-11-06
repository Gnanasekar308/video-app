package com.videoplayer.app.ui.player

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.videoplayer.app.data.local.LocalStore
import com.videoplayer.app.player.AspectRatioMode
import com.videoplayer.app.player.PlayerManager
import com.videoplayer.app.ui.theme.*
import com.videoplayer.app.utils.Formatters
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Player UI. States:
 *  1) Controls visible (top bar / center controls / bottom bar)
 *  2) Settings sheet (audio, subtitles, speed, background music)
 *  3) Gesture guidance overlay (first launch)
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    manager: PlayerManager, activity: PlayerActivity,
    uris: List<Uri>, ids: List<Long>, startIndex: Int, isVault: Boolean, onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }
    val exo = manager.exo
    val audio = remember { ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var controlsVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(!ctx.getSharedPreferences("player_ui", 0).getBoolean("guide_seen", false)) }
    var aspect by remember { mutableStateOf(AspectRatioMode.FIT) }
    var fullscreen by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var title by remember { mutableStateOf("") }
    var hud by remember { mutableStateOf<String?>(null) }

    // Start playback + resume
    LaunchedEffect(Unit) {
        val resume = ids.getOrNull(startIndex)?.let { store.position(it) } ?: 0L
        manager.setQueue(uris, startIndex, resume)
    }
    // Listener + position polling (also persists resume point)
    DisposableEffect(Unit) {
        val l = object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) { isPlaying = p }
            override fun onMediaItemTransition(m: androidx.media3.common.MediaItem?, r: Int) {
                title = uris.getOrNull(exo.currentMediaItemIndex)?.lastPathSegment ?: ""
            }
        }
        exo.addListener(l)
        onDispose {
            if (!isVault) ids.getOrNull(exo.currentMediaItemIndex)?.let { store.savePosition(it, exo.currentPosition) }
            exo.removeListener(l)
        }
    }
    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            position = exo.currentPosition; duration = exo.duration.coerceAtLeast(0)
            if (++tick % 10 == 0 && !isVault) ids.getOrNull(exo.currentMediaItemIndex)?.let { store.savePosition(it, position) }
            delay(500)
        }
    }
    // Auto-hide controls
    LaunchedEffect(controlsVisible, isPlaying, showSettings) {
        if (controlsVisible && isPlaying && !showSettings) { delay(3500); controlsVisible = false }
    }
    LaunchedEffect(hud) { if (hud != null) { delay(900); hud = null } }

    var size by remember { mutableStateOf(IntSize.Zero) }
    var dragStartX by remember { mutableStateOf(0f) }
    var axis by remember { mutableStateOf(0) }       // 0 undecided, 1 horizontal, 2 vertical
    var seekTarget by remember { mutableStateOf(0L) }

    Box(
        Modifier.fillMaxSize().background(VpBlack)
            .pointerInput(locked) {
                if (locked) { detectTapGestures(onTap = { controlsVisible = !controlsVisible }); return@pointerInput }
                detectTapGestures(
                    onTap = { controlsVisible = !controlsVisible },
                    onDoubleTap = { o ->                      // double tap left/right = -/+ 10s
                        if (o.x < size.width / 2) { manager.seekBy(-10_000); hud = "⏪ 10s" } else { manager.seekBy(10_000); hud = "10s ⏩" }
                    })
            }
            .pointerInput(locked) {
                if (locked) return@pointerInput
                detectDragGestures(
                    onDragStart = { dragStartX = it.x; axis = 0; seekTarget = exo.currentPosition },
                    onDragEnd = { if (axis == 1) exo.seekTo(seekTarget.coerceIn(0, duration)) }
                ) { change, drag ->
                    change.consume()
                    if (axis == 0) axis = if (abs(drag.x) > abs(drag.y)) 1 else 2
                    if (axis == 1) {                          // horizontal swipe = seek
                        seekTarget += (drag.x * 60).toLong()
                        hud = Formatters.duration(seekTarget.coerceIn(0, duration))
                    } else if (dragStartX < size.width / 2) { // left side = brightness
                        val lp = activity.window.attributes
                        val cur = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness
                        lp.screenBrightness = (cur - drag.y / size.height).coerceIn(0.02f, 1f)
                        activity.window.attributes = lp
                        hud = "☀ ${(lp.screenBrightness * 100).toInt()}%"
                    } else {                                  // right side = volume
                        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        val cur = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val step = if (drag.y < 0) 1 else -1
                        if (abs(drag.y) > 6) audio.setStreamVolume(AudioManager.STREAM_MUSIC, (cur + step).coerceIn(0, max), 0)
                        hud = "🔊 ${audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / max}%"
                    }
                }
            }
            .onSizeChangedCompat { size = it }
    ) {
        // Video surface
        AndroidView(factory = { c ->
            PlayerView(c).apply { player = exo; useController = false; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) }
        }, update = { v ->
            // Original maps to fixed width (native proportions); Zoom crops to fill.
            v.resizeMode = when (aspect) {
                AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
            v.subtitleView?.setFractionalTextSize(0.0533f * store.subtitleScale)
        }, modifier = Modifier.fillMaxSize())

        hud?.let {
            Text(it, color = VpWhite, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(.6f), MaterialTheme.shapes.large).padding(16.dp))
        }

        if (locked) {
            // Lock mode: only an unlock button is available.
            if (controlsVisible) IconButton({ locked = false }, Modifier.align(Alignment.CenterStart).padding(16.dp)
                .background(VpLightBlue, MaterialTheme.shapes.large)) { Icon(Icons.Default.LockOpen, "Unlock", tint = VpBlack) }
        } else if (controlsVisible) {
            // ---- Step 1: controls ----
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(Color.Black.copy(.45f)).statusBarsPadding().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = VpWhite) }
                Text(title, color = VpWhite, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton({ locked = true }) { Icon(Icons.Default.Lock, "Lock", tint = VpWhite) }
                IconButton({ showSettings = true }) { Icon(Icons.Default.MoreVert, "More", tint = VpWhite) }
            }
            Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ manager.previous() }) { Icon(Icons.Default.SkipPrevious, "Previous", tint = VpWhite) }
                IconButton({ manager.seekBy(-10_000) }) { Icon(Icons.Default.Replay10, "Rewind 10s", tint = VpWhite, modifier = Modifier.size(36.dp)) }
                FilledIconButton({ manager.togglePlay() }, Modifier.size(68.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = VpLightBlue, contentColor = VpBlack)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", modifier = Modifier.size(38.dp))
                }
                IconButton({ manager.seekBy(10_000) }) { Icon(Icons.Default.Forward10, "Forward 10s", tint = VpWhite, modifier = Modifier.size(36.dp)) }
                IconButton({ manager.next() }) { Icon(Icons.Default.SkipNext, "Next", tint = VpWhite) }
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(.45f)).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Formatters.duration(position), color = VpWhite, style = MaterialTheme.typography.labelSmall)
                    Slider(value = if (duration > 0) position.toFloat() / duration else 0f,
                        onValueChange = { exo.seekTo((it * duration).toLong()) }, modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = VpLightBlue, activeTrackColor = VpLightBlue))
                    Text(Formatters.duration(duration), color = VpWhite, style = MaterialTheme.typography.labelSmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton({ showSettings = true }) { Icon(Icons.Default.Subtitles, "Subtitles", tint = VpWhite) }
                    TextButton({ showSettings = true }) { Text("${exo.playbackParameters.speed}x", color = VpWhite) }
                    IconButton({ showSettings = true }) { Icon(Icons.Default.MusicNote, "Background Music", tint = VpLightBlue) }
                    TextButton({ aspect = AspectRatioMode.values()[(aspect.ordinal + 1) % 4] }) { Text(aspect.label, color = VpWhite) }
                    IconButton({ fullscreen = !fullscreen; activity.setFullscreen(fullscreen); activity.toggleOrientation() }) {
                        Icon(if (fullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Fullscreen", tint = VpWhite)
                    }
                }
            }
        }

        // ---- Step 3: gesture guidance overlay ----
        if (showGuide) GestureGuideOverlay {
            showGuide = false
            ctx.getSharedPreferences("player_ui", 0).edit().putBoolean("guide_seen", true).apply()
        }
    }

    // ---- Step 2: audio & subtitle settings ----
    if (showSettings) PlayerSettingsSheet(manager, store, onDismiss = { showSettings = false })
}

private fun Modifier.onSizeChangedCompat(cb: (IntSize) -> Unit) =
    this.then(Modifier.onSizeChanged(cb))
