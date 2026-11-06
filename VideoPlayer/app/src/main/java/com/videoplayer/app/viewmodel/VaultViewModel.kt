package com.videoplayer.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.videoplayer.app.vault.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VaultViewModel(app: Application) : AndroidViewModel(app) {
    private val pin = PinStore(app)
    private val manager = VaultManager(app)
    val unlocked: StateFlow<Boolean> = VaultSession.unlocked
    private val _items = MutableStateFlow<List<VaultVideo>>(emptyList())
    val items: StateFlow<List<VaultVideo>> = _items
    val error = MutableStateFlow<String?>(null)

    fun hasPin() = pin.hasPin()

    fun createPin(p: String) { if (p.length >= 4) { pin.setPin(p); VaultSession.unlock(); reload() } else error.value = "PIN must be at least 4 digits" }
    fun unlockWithPin(p: String) { if (pin.verify(p)) { VaultSession.unlock(); error.value = null; reload() } else error.value = "Wrong PIN" }
    fun unlockWithBiometric() { VaultSession.unlock(); reload() } // call only after BiometricHelper success
    fun lock() { VaultSession.lock(); manager.clearPlaybackCache() }

    fun reload() { _items.value = manager.list() }
    val busy = MutableStateFlow(false)

    fun import(uri: Uri) {
        viewModelScope.launch {
            busy.value = true
            runCatching { withContext(Dispatchers.IO) { manager.importVideo(uri) } }
                .onFailure { error.value = "Import failed: ${it.message}" }
            busy.value = false
            reload()
        }
    }

    /** Decrypts off the main thread, then calls [onReady] on the main thread. */
    fun preparePlayback(v: VaultVideo, onReady: (File) -> Unit) {
        viewModelScope.launch {
            busy.value = true
            runCatching { withContext(Dispatchers.IO) { manager.prepareForPlayback(v) } }
                .onSuccess(onReady)
                .onFailure { error.value = "Cannot open: ${it.message}" }
            busy.value = false
        }
    }
    /** Export requires re-authentication: pass the PIN again. */
    fun export(v: VaultVideo, target: Uri, reauthPin: String): Boolean {
        if (!pin.verify(reauthPin)) { error.value = "Wrong PIN"; return false }
        manager.export(v, target); return true
    }
    fun delete(v: VaultVideo) { manager.delete(v); reload() }
}
