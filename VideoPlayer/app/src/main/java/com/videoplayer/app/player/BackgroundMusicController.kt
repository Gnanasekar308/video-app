package com.videoplayer.app.player

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

/**
 * Background Music ON/OFF.
 *  ON  -> original audio track.
 *  OFF -> only if a dialogue-focused track exists (switching architecture). Otherwise returns Unsupported.
 * EXTENSION POINT: add a DSP/AudioProcessor-based implementation here later.
 */
class BackgroundMusicController(private val player: ExoPlayer, private val tracks: TrackHelper) {

    private val dialogueHints = listOf("dialogue", "dialog", "voice", "vocals", "no music", "commentary-off", "speech")
    private var originalTrack: TrackOption? = null

    fun isRemovalSupported() = findDialogueTrack() != null

    private fun findDialogueTrack() =
        tracks.audioTracks().firstOrNull { t -> dialogueHints.any { t.label.contains(it, ignoreCase = true) } }

    fun setMusicEnabled(enabled: Boolean): MusicResult {
        val all = tracks.audioTracks()
        if (enabled) {
            (originalTrack ?: all.firstOrNull())?.let { tracks.select(C.TRACK_TYPE_AUDIO, it) }
            return MusicResult.Enabled
        }
        val dialogue = findDialogueTrack()
            ?: return MusicResult.Unsupported("Full music removal may not be available: only a mixed audio track was found.")
        originalTrack = all.firstOrNull { it.selected }
        tracks.select(C.TRACK_TYPE_AUDIO, dialogue)
        return MusicResult.RemovedViaTrack
    }
}
