package com.videoplayer.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.videoplayer.app.ui.theme.*

/** Step 3: first-run overlay explaining gestures. Tap anywhere to dismiss. */
@Composable
fun GestureGuideOverlay(onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.78f)).clickable { onDismiss() }, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Gesture Controls", style = MaterialTheme.typography.titleLarge, color = VpLightBlue)
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column { Text("◀ Left side", color = VpLightBlue); Text("Swipe ↕  Brightness"); Text("Double tap  Rewind 10s") }
                Column { Text("Right side ▶", color = VpLightBlue); Text("Swipe ↕  Volume"); Text("Double tap  Forward 10s") }
            }
            Text("Swipe ↔  Seek     •     Tap  Show/hide controls", color = VpWhite)
            Text("🔒 Lock button prevents accidental touches", color = VpWhite)
            Spacer(Modifier.height(8.dp))
            Text("Tap to continue", style = MaterialTheme.typography.labelSmall)
        }
    }
}
