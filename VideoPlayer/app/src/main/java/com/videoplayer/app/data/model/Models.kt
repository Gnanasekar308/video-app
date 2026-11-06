package com.videoplayer.app.data.model

import android.net.Uri

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val folder: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val width: Int = 0,
    val height: Int = 0,
    val resumePositionMs: Long = 0L
) {
    val progress: Float get() = if (durationMs > 0) (resumePositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val resolutionLabel: String get() = if (height > 0) "${height}p" else ""
}

data class VideoFolder(val name: String, val videoCount: Int, val videos: List<VideoItem>)

data class Playlist(val id: String, val name: String, val videoIds: List<Long>)

enum class SortOption { NAME, DATE, DURATION, SIZE }
