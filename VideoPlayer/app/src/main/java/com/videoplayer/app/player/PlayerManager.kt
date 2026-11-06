package com.videoplayer.app.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/** Thin wrapper around ExoPlayer: playlist, speed, tracks, external SRT, resume. */
class PlayerManager(context: Context) {
    val exo: ExoPlayer = ExoPlayer.Builder(context).build()
    val tracks = TrackHelper(exo)
    val music = BackgroundMusicController(exo, tracks)

    fun setQueue(uris: List<Uri>, startIndex: Int, startPositionMs: Long) {
        exo.setMediaItems(uris.map { MediaItem.fromUri(it) }, startIndex, startPositionMs)
        exo.prepare()
        exo.playWhenReady = true
    }

    fun togglePlay() { if (exo.isPlaying) exo.pause() else exo.play() }
    fun seekBy(deltaMs: Long) = exo.seekTo((exo.currentPosition + deltaMs).coerceIn(0, exo.duration.coerceAtLeast(0)))
    fun next() { if (exo.hasNextMediaItem()) exo.seekToNextMediaItem() }
    fun previous() { if (exo.hasPreviousMediaItem()) exo.seekToPreviousMediaItem() }
    fun setSpeed(speed: Float) { exo.playbackParameters = PlaybackParameters(speed) }

    /** Attach an external .srt to the current item (call before/after prepare). */
    fun addExternalSubtitle(videoUri: Uri, srtUri: Uri, language: String = "und") {
        val sub = MediaItem.SubtitleConfiguration.Builder(srtUri)
            .setMimeType(MimeTypes.APPLICATION_SUBRIP).setLanguage(language)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build()
        val pos = exo.currentPosition
        exo.setMediaItem(MediaItem.Builder().setUri(videoUri).setSubtitleConfigurations(listOf(sub)).build(), pos)
        exo.prepare()
    }

    fun release() = exo.release()
}
