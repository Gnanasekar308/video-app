package com.videoplayer.app.vault

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Optional biometric unlock (structure only; PIN remains the fallback). */
object BiometricHelper {
    fun isAvailable(activity: FragmentActivity) =
        BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

    fun prompt(activity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit = {}) {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault").setNegativeButtonText("Use PIN").build()
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) = onSuccess()
                override fun onAuthenticationError(code: Int, msg: CharSequence) = onFail()
            }).authenticate(info)
    }
}
