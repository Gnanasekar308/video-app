package com.videoplayer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.videoplayer.app.data.local.LocalStore
import com.videoplayer.app.data.model.*
import com.videoplayer.app.data.repository.UserDataRepository
import com.videoplayer.app.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LibraryState(
    val all: List<VideoItem> = emptyList(),
    val continueWatching: List<VideoItem> = emptyList(),
    val recent: List<VideoItem> = emptyList(),
    val favorites: List<VideoItem> = emptyList(),
    val folders: List<VideoFolder> = emptyList(),
    val query: String = "",
    val sort: SortOption = SortOption.DATE,
    val ascending: Boolean = false,
    val selectedFolder: String? = null
)

/** Shared by Home + Library screens. */
class LibraryViewModel(app: Application) : AndroidViewModel(app) {
    private val videos = VideoRepository(app)
    val userData = UserDataRepository(LocalStore(app))
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state

    fun refresh() = viewModelScope.launch {
        val all = userData.withResume(videos.loadAll())
        val favIds = userData.favoriteIds()
        _state.value = _state.value.copy(
            all = all,
            continueWatching = userData.continueWatching(all),
            recent = all.take(10),
            favorites = all.filter { it.id in favIds },
            folders = videos.folders(all)
        )
    }

    fun setQuery(q: String) { _state.value = _state.value.copy(query = q) }
    fun setSort(s: SortOption, asc: Boolean) { _state.value = _state.value.copy(sort = s, ascending = asc) }
    fun selectFolder(name: String?) { _state.value = _state.value.copy(selectedFolder = name) }
    fun toggleFavorite(id: Long) { userData.toggleFavorite(id); refresh() }

    /** Filtered + sorted list for the Library screen. */
    fun visible(): List<VideoItem> {
        val s = _state.value
        val base = s.selectedFolder?.let { f -> s.all.filter { it.folder == f } } ?: s.all
        return videos.sort(videos.search(base, s.query), s.sort, s.ascending)
    }
}
