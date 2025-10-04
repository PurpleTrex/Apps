package com.example.notepad.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

class BiometricHelper(context: Context) {
    private val ctxRef = WeakReference(context)

    fun canAuth(): Boolean {
        val ctx = ctxRef.get() ?: return false
        val bm = BiometricManager.from(ctx)
        return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun prompt(title: String, subtitle: String? = null, onSuccess: () -> Unit, onFail: () -> Unit) {
        val ctx = ctxRef.get() ?: return onFail()
        if (ctx !is FragmentActivity || !canAuth()) { onSuccess(); return } // fallback: auto success
        val executor = ContextCompat.getMainExecutor(ctx)
        val prompt = BiometricPrompt(ctx, executor, object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onFail() }
            override fun onAuthenticationFailed() { onFail() }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }
}
