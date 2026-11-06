package com.videoplayer.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.videoplayer.app.data.model.SortOption
import com.videoplayer.app.data.model.VideoItem
import com.videoplayer.app.ui.components.VideoCard
import com.videoplayer.app.viewmodel.LibraryViewModel

/** Library: folder browsing, filename search, sorting (name/date/duration/size). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: LibraryViewModel, onPlay: (List<VideoItem>, Int) -> Unit) {
    val s by vm.state.collectAsState()
    var menu by remember { mutableStateOf(false) }
    val list = vm.visible()

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = s.query, onValueChange = vm::setQuery, singleLine = true,
                placeholder = { Text("Search by filename") }, modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.Sort, "Sort") }
                DropdownMenu(menu, { menu = false }) {
                    SortOption.values().forEach { opt ->
                        DropdownMenuItem(text = { Text(opt.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = { vm.setSort(opt, !(s.sort == opt && s.ascending)); menu = false })
                    }
                }
            }
        }
        if (s.selectedFolder != null) {
            TextButton(onClick = { vm.selectFolder(null) }) { Text("‹ ${s.selectedFolder}  (show all)") }
        }
        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(list.size) { i ->
                VideoCard(list[i], 170.dp, 100.dp, onClick = { onPlay(list, i) }, onMore = { vm.toggleFavorite(list[i].id) })
            }
        }
    }
}
