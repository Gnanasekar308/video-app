package com.videoplayer.app.vault

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory unlock state. Reset when the app is backgrounded (see VideoPlayerApp). */
object VaultSession {
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked
    fun unlock() { _unlocked.value = true }
    fun lock() { _unlocked.value = false }
}
