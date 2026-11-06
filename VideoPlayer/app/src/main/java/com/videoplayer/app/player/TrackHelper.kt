package com.videoplayer.app.player

import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer

/** Reads embedded audio/subtitle tracks and applies selection. */
class TrackHelper(private val player: ExoPlayer) {

    fun audioTracks() = list(C.TRACK_TYPE_AUDIO)
    fun subtitleTracks() = list(C.TRACK_TYPE_TEXT)

    private fun list(type: Int): List<TrackOption> {
        val out = mutableListOf<TrackOption>()
        player.currentTracks.groups.forEachIndexed { gi, g ->
            if (g.type != type) return@forEachIndexed
            for (ti in 0 until g.length) {
                val f = g.getTrackFormat(ti)
                val label = f.label ?: f.language?.let { java.util.Locale(it).displayLanguage } ?: "Track ${out.size + 1}"
                out += TrackOption(label, gi, ti, g.isTrackSelected(ti))
            }
        }
        return out
    }

    fun select(type: Int, option: TrackOption) {
        val group = player.currentTracks.groups[option.groupIndex].mediaTrackGroup
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(type, false)
            .setOverrideForType(TrackSelectionOverride(group, option.trackIndex)).build()
    }

    fun setSubtitlesEnabled(enabled: Boolean) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled).build()
    }
}
