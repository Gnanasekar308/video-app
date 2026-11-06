package com.videoplayer.app.vault

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

/** Stores a salted SHA-256 hash of the PIN. TODO: upgrade to PBKDF2/Argon2 + Keystore-wrapped secret. */
class PinStore(context: Context) {
    private val prefs = context.getSharedPreferences("vault_pin", Context.MODE_PRIVATE)

    fun hasPin() = prefs.contains("hash")

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString("salt", salt.toHex()).putString("hash", hash(pin, salt)).apply()
    }

    fun verify(pin: String): Boolean {
        val salt = prefs.getString("salt", null)?.fromHex() ?: return false
        return hash(pin, salt) == prefs.getString("hash", null)
    }

    private fun hash(pin: String, salt: ByteArray) =
        MessageDigest.getInstance("SHA-256").digest(salt + pin.toByteArray()).toHex()

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
    private fun String.fromHex() = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
