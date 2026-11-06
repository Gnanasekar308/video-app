package com.videoplayer.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.videoplayer.app.data.local.LocalStore

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val store = remember { LocalStore(ctx) }
    var biometric by remember { mutableStateOf(store.biometricEnabled) }
    var subScale by remember { mutableStateOf(store.subtitleScale) }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Row { Text("Vault biometric unlock", Modifier.weight(1f)); Switch(biometric, { biometric = it; store.biometricEnabled = it }) }
        Text("Default subtitle size: ${"%.1f".format(subScale)}x")
        Slider(subScale, { subScale = it; store.subtitleScale = it }, valueRange = 0.75f..2f)
        TextButton({ store.clearHistory() }) { Text("Clear watch history") }
        Text("VideoPlayer 1.0.0 • local only • no network", style = MaterialTheme.typography.labelSmall)
    }
}
