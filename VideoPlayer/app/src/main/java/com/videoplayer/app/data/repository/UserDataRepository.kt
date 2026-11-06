package com.videoplayer.app.data.repository

import com.videoplayer.app.data.local.LocalStore
import com.videoplayer.app.data.model.*
import java.util.UUID

/** Favorites, history, continue watching and local playlists. */
class UserDataRepository(private val store: LocalStore) {
    fun favoriteIds() = store.favorites()
    fun toggleFavorite(id: Long) = store.setFavorite(id, id !in store.favorites())

    fun savePosition(id: Long, posMs: Long) = store.savePosition(id, posMs)
    fun position(id: Long) = store.position(id)

    fun withResume(videos: List<VideoItem>) = videos.map { it.copy(resumePositionMs = store.position(it.id)) }

    /** Videos started but not finished (resume point between 1s and 95%). */
    fun continueWatching(videos: List<VideoItem>): List<VideoItem> =
        store.historyIds().mapNotNull { id -> videos.firstOrNull { it.id == id } }
            .map { it.copy(resumePositionMs = store.position(it.id)) }
            .filter { it.resumePositionMs > 1000 && it.progress < 0.95f }

    fun history(videos: List<VideoItem>) = store.historyIds().mapNotNull { id -> videos.firstOrNull { it.id == id } }
    fun clearHistory() = store.clearHistory()

    fun playlists(): List<Playlist> = store.playlists().map { Playlist(it.first, it.second.first, it.second.second) }
    fun createPlaylist(name: String) {
        val all = playlists().map { Triple(it.id, it.name, it.videoIds) } + Triple(UUID.randomUUID().toString(), name, emptyList<Long>())
        store.savePlaylists(all)
    }
    fun addToPlaylist(playlistId: String, videoId: Long) {
        store.savePlaylists(playlists().map {
            Triple(it.id, it.name, if (it.id == playlistId) (it.videoIds + videoId).distinct() else it.videoIds)
        })
    }
}
