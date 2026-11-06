package com.videoplayer.app.vault

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.videoplayer.app.vault.VaultCrypto
import java.io.File

data class VaultVideo(val name: String, val file: File)

/** Imports, lists, decrypts (for playback) and exports vault videos. App-private storage only. */
class VaultManager(private val context: Context) {
    private val dir = File(context.filesDir, "vault").apply { mkdirs() }

    fun list(): List<VaultVideo> =
        dir.listFiles()?.filter { it.extension == "vault" }?.map { VaultVideo(it.nameWithoutExtension, it) } ?: emptyList()

    fun importVideo(uri: Uri) {
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && i >= 0) c.getString(i) else null
        } ?: "video_${System.currentTimeMillis()}.mp4"
        context.contentResolver.openInputStream(uri)?.use { input ->
            File(dir, "$name.vault").outputStream().use { VaultCrypto.encrypt(input, it) }
        }
    }

    /** Decrypts to cache for playback. TODO: replace with streaming DataSource and wipe cache on lock. */
    fun prepareForPlayback(v: VaultVideo): File {
        val tmp = File(context.cacheDir, "vault_play_${v.name}")
        v.file.inputStream().use { i -> tmp.outputStream().use { VaultCrypto.decrypt(i, it) } }
        return tmp
    }

    /** Caller must have verified authentication (PIN/biometric) before invoking. */
    fun export(v: VaultVideo, target: Uri) {
        v.file.inputStream().use { i ->
            context.contentResolver.openOutputStream(target)?.use { VaultCrypto.decrypt(i, it) }
        }
    }

    fun delete(v: VaultVideo) = v.file.delete()
    fun clearPlaybackCache() = context.cacheDir.listFiles()?.filter { it.name.startsWith("vault_play_") }?.forEach { it.delete() }
}
