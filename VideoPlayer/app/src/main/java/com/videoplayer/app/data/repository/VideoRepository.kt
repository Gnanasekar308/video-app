package com.videoplayer.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.videoplayer.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Discovers local videos via MediaStore. Vault files live in private storage so never appear here. */
class VideoRepository(private val context: Context) {

    suspend fun loadAll(): List<VideoItem> = withContext(Dispatchers.IO) {
        val out = mutableListOf<VideoItem>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf(
            MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT
        )
        context.contentResolver.query(collection, proj, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                out += VideoItem(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id),
                    title = c.getString(1) ?: "Unknown",
                    folder = c.getString(2) ?: "Unknown",
                    durationMs = c.getLong(3), sizeBytes = c.getLong(4), dateAddedSec = c.getLong(5),
                    width = c.getInt(6), height = c.getInt(7)
                )
            }
        }
        out
    }

    fun folders(videos: List<VideoItem>): List<VideoFolder> =
        videos.groupBy { it.folder }.map { (n, v) -> VideoFolder(n, v.size, v) }.sortedBy { it.name.lowercase() }

    fun search(videos: List<VideoItem>, query: String) =
        if (query.isBlank()) videos else videos.filter { it.title.contains(query, ignoreCase = true) }

    fun sort(videos: List<VideoItem>, option: SortOption, ascending: Boolean = true): List<VideoItem> {
        val sorted = when (option) {
            SortOption.NAME -> videos.sortedBy { it.title.lowercase() }
            SortOption.DATE -> videos.sortedBy { it.dateAddedSec }
            SortOption.DURATION -> videos.sortedBy { it.durationMs }
            SortOption.SIZE -> videos.sortedBy { it.sizeBytes }
        }
        return if (ascending) sorted else sorted.reversed()
    }
}
