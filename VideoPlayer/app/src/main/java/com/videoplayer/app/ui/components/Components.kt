package com.videoplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.videoplayer.app.data.model.VideoFolder
import com.videoplayer.app.data.model.VideoItem
import com.videoplayer.app.ui.theme.*
import com.videoplayer.app.utils.Formatters

/** Section title row with "See All >" action. */
@Composable
fun SectionHeader(title: String, icon: @Composable () -> Unit, onSeeAll: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        icon(); Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Row(Modifier.clickable { onSeeAll() }, verticalAlignment = Alignment.CenterVertically) {
            Text("See All", color = VpLightBlue, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.ChevronRight, null, tint = VpLightBlue, modifier = Modifier.size(16.dp))
        }
    }
}

/** Rounded video card: thumbnail, duration badge, optional play overlay + progress, title + meta. */
@Composable
fun VideoCard(
    video: VideoItem, width: Dp, thumbHeight: Dp,
    showProgress: Boolean = false, onClick: () -> Unit, onMore: () -> Unit = {}
) {
    Column(Modifier.width(width).clickable { onClick() }) {
        Box(Modifier.fillMaxWidth().height(thumbHeight).clip(RoundedCornerShape(16.dp)).background(VpCard)) {
            AsyncImage(video.uri, video.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (showProgress) {
                Box(Modifier.align(Alignment.Center).size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = .55f)),
                    contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, null, tint = VpWhite) }
                LinearProgressIndicator(progress = { video.progress }, color = VpLightBlue, trackColor = Color.White.copy(alpha = .25f),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp))
            }
            Text(Formatters.duration(video.durationMs), color = VpWhite, style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = .65f)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(video.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                Text("${video.resolutionLabel} • ${Formatters.size(video.sizeBytes)}", style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Default.MoreVert, null, tint = VpMuted, modifier = Modifier.size(18.dp).clickable { onMore() })
        }
    }
}

@Composable
fun FolderCard(folder: VideoFolder, onClick: () -> Unit) {
    Column(
        Modifier.width(110.dp).clip(RoundedCornerShape(16.dp)).background(VpCard).clickable { onClick() }.padding(12.dp)
    ) {
        Icon(Icons.Default.Folder, null, tint = VpLightBlue, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(8.dp))
        Text(folder.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
        Text("${folder.videoCount} videos", style = MaterialTheme.typography.labelSmall)
    }
}
