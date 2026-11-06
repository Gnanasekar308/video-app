package com.videoplayer.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videoplayer.app.data.model.VideoItem
import com.videoplayer.app.ui.components.*
import com.videoplayer.app.ui.theme.VpLightBlue
import com.videoplayer.app.viewmodel.LibraryViewModel

/** Home: Continue Watching, Recent, Favorites, Folders + header with search & vault shortcut. */
@Composable
fun HomeScreen(
    vm: LibraryViewModel,
    onPlay: (List<VideoItem>, Int) -> Unit,
    onSearch: () -> Unit, onVault: () -> Unit, onSeeAllLibrary: () -> Unit,
    onOpenFolder: (String) -> Unit
) {
    val s by vm.state.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PlayCircle, null, tint = VpLightBlue, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(10.dp))
            Text("VideoPlayer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, "Search") }
            IconButton(onClick = onVault) { Icon(Icons.Outlined.Lock, "Vault", tint = VpLightBlue) }
        }

        if (s.continueWatching.isNotEmpty()) {
            SectionHeader("Continue Watching", { Icon(Icons.Outlined.PlayCircle, null, tint = VpLightBlue) }, onSeeAllLibrary)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(s.continueWatching.size) { i ->
                    VideoCard(s.continueWatching[i], 240.dp, 130.dp, showProgress = true, onClick = { onPlay(s.continueWatching, i) })
                }
            }
        }

        SectionHeader("Recent Videos", { Icon(Icons.Outlined.History, null) }, onSeeAllLibrary)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(s.recent.size) { i -> VideoCard(s.recent[i], 150.dp, 90.dp, onClick = { onPlay(s.recent, i) }) }
        }

        SectionHeader("Favorites", { Icon(Icons.Outlined.FavoriteBorder, null) }, onSeeAllLibrary)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(s.favorites.size) { i -> VideoCard(s.favorites[i], 150.dp, 90.dp, onClick = { onPlay(s.favorites, i) }) }
        }

        SectionHeader("Folders", { Icon(Icons.Outlined.FolderOpen, null) }, onSeeAllLibrary)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(s.folders) { f -> FolderCard(f) { onOpenFolder(f.name) } }
        }
        Spacer(Modifier.height(24.dp))
    }
}
