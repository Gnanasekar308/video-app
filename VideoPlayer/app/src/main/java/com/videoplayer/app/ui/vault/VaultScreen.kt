package com.videoplayer.app.ui.vault

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.videoplayer.app.ui.player.PlayerActivity
import com.videoplayer.app.ui.theme.VpLightBlue
import com.videoplayer.app.viewmodel.VaultViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

/** Vault: PIN gate -> list of private videos (hidden from normal library). */
@Composable
fun VaultScreen(vm: VaultViewModel) {
    val unlocked by vm.unlocked.collectAsState()
    val error by vm.error.collectAsState()
    if (!unlocked) PinGate(vm.hasPin(), error, vm::createPin, vm::unlockWithPin) else VaultContent(vm)
}

@Composable
private fun PinGate(hasPin: Boolean, error: String?, onCreate: (String) -> Unit, onUnlock: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Lock, null, tint = VpLightBlue, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(if (hasPin) "Enter vault PIN" else "Create a vault PIN", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(8) }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), isError = error != null, supportingText = { error?.let { Text(it) } })
        Spacer(Modifier.height(16.dp))
        Button({ if (hasPin) onUnlock(pin) else onCreate(pin); pin = "" }, shape = RoundedCornerShape(14.dp)) {
            Text(if (hasPin) "Unlock" else "Create PIN")
        }
        // TODO: biometric button -> BiometricHelper.prompt(...) when enabled in Settings
    }
}

@Composable
private fun VaultContent(vm: VaultViewModel) {
    val items by vm.items.collectAsState()
    val busy by vm.busy.collectAsState()
    val error by vm.error.collectAsState()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { vm.reload() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.import(it) } // TODO: offer "delete original after import"
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Private Vault", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton({ vm.lock() }) { Text("Lock") }
        }
        Button({ picker.launch(arrayOf("video/*")) }, enabled = !busy, shape = RoundedCornerShape(14.dp)) { Text("Import video") }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp), color = VpLightBlue)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { v ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(v.name, Modifier.weight(1f))
                        TextButton({ // Vault playback screen = PlayerActivity in vault mode
                            vm.preparePlayback(v) { f ->
                                ctx.startActivity(Intent(ctx, PlayerActivity::class.java)
                                    .putStringArrayListExtra(PlayerActivity.EXTRA_URIS, arrayListOf(f.toUri().toString()))
                                    .putExtra(PlayerActivity.EXTRA_VAULT, true))
                            }
                        }, enabled = !busy) { Text("Play") }
                        TextButton({ /* TODO: re-auth dialog then vm.export(v, target, pin) */ }) { Text("Export") }
                        TextButton({ vm.delete(v) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
