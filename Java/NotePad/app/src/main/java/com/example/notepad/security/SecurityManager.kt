package com.example.notepad.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages app-wide security including:
 * - PIN/password authentication
 * - Distress code (triggers app destruction)
 * - Note locking with salted hashing
 * - First-time setup
 */
object SecurityManager {
    private const val TAG = "SecurityManager"
    private const val PREFS_NAME = "security_prefs"
    private const val KEY_HAS_SETUP = "has_setup"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_DISTRESS_HASH = "distress_hash"
    private const val KEY_SALT = "salt"
    private const val KEY_LOCK_ENABLED = "lock_enabled"
    
    private var cachedPrefs: SharedPreferences? = null
    
    /**
     * Get encrypted preferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        if (cachedPrefs != null) return cachedPrefs!!
        
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        cachedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        return cachedPrefs!!
    }
    
    /**
     * Check if initial setup is complete
     */
    fun hasCompletedSetup(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_SETUP, false)
    }
    
    /**
     * Check if lock is enabled
     */
    fun isLockEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOCK_ENABLED, false)
    }
    
    /**
     * Complete initial setup with PIN and distress code
     */
    fun completeSetup(context: Context, pin: String?, distressCode: String?) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        
        if (pin != null && distressCode != null) {
            val salt = generateSalt()
            editor.putString(KEY_SALT, salt)
            editor.putString(KEY_PIN_HASH, hashPassword(pin, salt))
            editor.putString(KEY_DISTRESS_HASH, hashPassword(distressCode, salt))
            editor.putBoolean(KEY_LOCK_ENABLED, true)
        } else {
            editor.putBoolean(KEY_LOCK_ENABLED, false)
        }
        
        editor.putBoolean(KEY_HAS_SETUP, true)
        editor.apply()
        
        Log.d(TAG, "Setup completed. Lock enabled: ${pin != null}")
    }
    
    /**
     * Verify PIN or password
     * Returns: 0 = invalid, 1 = correct, 2 = distress code
     */
    fun verifyPin(context: Context, input: String): Int {
        if (!isLockEnabled(context)) return 1 // No lock, always pass
        
        val prefs = getPrefs(context)
        val salt = prefs.getString(KEY_SALT, "") ?: ""
        val pinHash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        val distressHash = prefs.getString(KEY_DISTRESS_HASH, "") ?: ""
        
        val inputHash = hashPassword(input, salt)
        
        return when {
            inputHash == distressHash -> 2 // DISTRESS CODE!
            inputHash == pinHash -> 1      // Correct PIN
            else -> 0                       // Invalid
        }
    }
    
    /**
     * Hash password with salt using SHA-256
     */
    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest("$salt$password$salt".toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Generate random salt
     */
    private fun generateSalt(): String {
        val bytes = ByteArray(32)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Encrypt text with salted hash (for note contents)
     */
    fun encryptWithSaltedHash(plaintext: String, password: String): String {
        try {
            val salt = generateSalt()
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16)
            java.security.SecureRandom().nextBytes(iv)
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            
            val encrypted = cipher.doFinal(plaintext.toByteArray())
            
            // Format: salt:iv:encrypted (all hex encoded)
            return "${salt}:${iv.toHex()}:${encrypted.toHex()}"
        } catch (e: Exception) {
            Log.e(TAG, "Encryption error: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Decrypt salted hash encrypted text
     */
    fun decryptWithSaltedHash(encrypted: String, password: String): String? {
        try {
            val parts = encrypted.split(":")
            if (parts.size != 3) return null
            
            val salt = parts[0]
            val iv = parts[1].hexToByteArray()
            val ciphertext = parts[2].hexToByteArray()
            
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            
            val decrypted = cipher.doFinal(ciphertext)
            return String(decrypted)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error: ${e.message}")
            return null
        }
    }
    
    /**
     * Derive encryption key from password and salt
     */
    private fun deriveKey(password: String, salt: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest("$salt$password".toByteArray())
    }
    
    /**
     * Reset all security settings (used after distress code or manual reset)
     */
    suspend fun resetSecurity(context: Context) = withContext(Dispatchers.IO) {
        try {
            // Clear all app data
            AppDestructionManager.executeDestruction(context)
            
            // Clear security prefs
            getPrefs(context).edit().clear().apply()
            cachedPrefs = null
            
            Log.w(TAG, "Security reset completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during security reset: ${e.message}", e)
        }
    }
    
    // Extension functions for hex conversion
    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    
    private fun String.hexToByteArray(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
