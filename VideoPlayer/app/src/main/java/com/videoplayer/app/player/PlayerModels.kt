package com.videoplayer.app.player

enum class AspectRatioMode(val label: String) { FIT("Fit"), FILL("Fill"), ORIGINAL("Original"), ZOOM("Zoom") }

object PlaybackSpeeds { val all = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f) }

data class TrackOption(val label: String, val groupIndex: Int, val trackIndex: Int, val selected: Boolean)

sealed interface MusicResult {
    object Enabled : MusicResult                       // original audio
    object RemovedViaTrack : MusicResult               // dialogue-focused track selected
    data class Unsupported(val message: String) : MusicResult
}
