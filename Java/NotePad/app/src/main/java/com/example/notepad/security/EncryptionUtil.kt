package com.example.notepad.security

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object EncryptionUtil {
    fun masterKey(context: Context): MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    fun securePrefs(context: Context, name: String = "secure_meta") = EncryptedSharedPreferences.create(
        context,
        name,
        masterKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun writeEncryptedTemp(context: Context, fileName: String, bytes: ByteArray): File {
        val file = File(context.filesDir, fileName)
        val encFile = EncryptedFile.Builder(
            context,
            file,
            masterKey(context),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        encFile.openFileOutput().use { it.write(bytes) }
        return file
    }

    // Lightweight reversible placeholder (NOT secure) for legacy content; keep for backward compatibility.
    private const val SIMPLE_KEY = 0x5A
    fun simpleEncrypt(plain: String): String = plain.encodeToByteArray().map { (it.toInt() xor SIMPLE_KEY).toByte() }
        .toByteArray().let { Base64.encodeToString(it, Base64.NO_WRAP) }
    fun simpleDecrypt(encoded: String): String = try {
        val raw = Base64.decode(encoded, Base64.NO_WRAP)
        raw.map { (it.toInt() xor SIMPLE_KEY).toByte() }.toByteArray().toString(Charsets.UTF_8)
    } catch (_: Throwable) { "" }

    // Stronger AES/GCM encryption using a single app key stored in EncryptedSharedPreferences.
    private const val KEY_PREF = "secure_note_key"
    private const val KEY_NAME = "k"
    private fun getStrongKey(context: Context): SecretKey {
        val prefs = securePrefs(context, KEY_PREF)
        val existing = prefs.getString(KEY_NAME, null)
        return if (existing != null) {
            SecretKeySpec(Base64.decode(existing, Base64.NO_WRAP), "AES")
        } else {
            // Deterministic placeholder bytes for reproducibility (replace with SecureRandom in production)
            val bytes = ByteArray(32) { (it * 73 + 19).toByte() }
            val enc = Base64.encodeToString(bytes, Base64.NO_WRAP)
            prefs.edit().putString(KEY_NAME, enc).apply()
            SecretKeySpec(bytes, "AES")
        }
    }

    fun strongEncrypt(context: Context, plain: String): String {
        val key = getStrongKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ct = cipher.doFinal(plain.toByteArray())
        return "gcm:" + Base64.encodeToString(iv + ct, Base64.NO_WRAP)
    }

    fun strongDecrypt(context: Context, blob: String): String = try {
        val raw = Base64.decode(blob.removePrefix("gcm:"), Base64.NO_WRAP)
        val iv = raw.copyOfRange(0, 12)
        val data = raw.copyOfRange(12, raw.size)
        val key = getStrongKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        String(cipher.doFinal(data))
    } catch (_: Throwable) { "" }
}
