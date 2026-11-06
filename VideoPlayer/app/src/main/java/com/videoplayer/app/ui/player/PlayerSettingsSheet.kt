package com.videoplayer.app.ui.player

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import com.videoplayer.app.data.local.LocalStore
import com.videoplayer.app.player.*
import com.videoplayer.app.ui.theme.*

/** Audio Track / Subtitles / Playback Speed / Background Music ON-OFF. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerSettingsSheet(manager: PlayerManager, store: LocalStore, onDismiss: () -> Unit) {
    var musicOn by remember { mutableStateOf(true) }
    var musicNote by remember { mutableStateOf<String?>(null) }
    var subsOn by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(manager.exo.playbackParameters.speed) }
    var subScale by remember { mutableStateOf(store.subtitleScale) }
    val audioTracks = remember { manager.tracks.audioTracks() }
    val subTracks = remember { manager.tracks.subtitleTracks() }

    val srtPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { srt ->
        val current = manager.exo.currentMediaItem?.localConfiguration?.uri
        if (srt != null && current != null) manager.addExternalSubtitle(current, srt)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = VpSurface) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Audio Track", style = MaterialTheme.typography.titleMedium)
            if (audioTracks.isEmpty()) Text("No alternate audio tracks", style = MaterialTheme.typography.labelSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                audioTracks.forEach { t -> FilterChip(t.selected, { manager.tracks.select(C.TRACK_TYPE_AUDIO, t) }, { Text(t.label) }) }
            }

            Text("Subtitles", style = MaterialTheme.typography.titleMedium)
            Row { Text("Show subtitles", Modifier.weight(1f)); Switch(subsOn, { subsOn = it; manager.tracks.setSubtitlesEnabled(it) }) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                subTracks.forEach { t -> FilterChip(t.selected, { manager.tracks.select(C.TRACK_TYPE_TEXT, t) }, { Text(t.label) }) }
                AssistChip({ srtPicker.launch(arrayOf("application/x-subrip", "text/*", "application/octet-stream")) }, { Text("+ Add SRT") })
            }
            Text("Subtitle size ${"%.1f".format(subScale)}x", style = MaterialTheme.typography.labelSmall)
            Slider(subScale, { subScale = it; store.subtitleScale = it }, valueRange = 0.75f..2f)

            Text("Playback Speed", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaybackSpeeds.all.forEach { s -> FilterChip(speed == s, { speed = s; manager.setSpeed(s) }, { Text("${s}x") }) }
            }

            Text("Background Music", style = MaterialTheme.typography.titleMedium)
            Row { Text(if (musicOn) "ON (original audio)" else "OFF", Modifier.weight(1f))
                Switch(musicOn, { want ->
                    when (val r = manager.music.setMusicEnabled(want)) {
                        is MusicResult.Unsupported -> { musicOn = true; musicNote = r.message }   // revert: not supported
                        else -> { musicOn = want; musicNote = null }
                    }
                }, colors = SwitchDefaults.colors(checkedTrackColor = VpLightBlue))
            }
            musicNote?.let { Text(it, color = VpLightBlue, style = MaterialTheme.typography.bodyMedium) }
            Text("Music removal works only when a supported dialogue-focused track or processing is available.",
                style = MaterialTheme.typography.labelSmall)
        }
    }
}
