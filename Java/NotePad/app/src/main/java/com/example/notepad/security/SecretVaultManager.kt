package com.example.notepad.security

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * SecretVaultManager manages an isolated, encrypted file storage area.
 * Files are stored in app's internal storage with encryption.
 * No other apps can access these files.
 */
class SecretVaultManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SecretVault"
        private const val VAULT_DIR = "secret_vault"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_SIZE = 32 // 256 bits
    }
    
    data class VaultItem(
        val id: String,
        val fileName: String,
        val type: VaultItemType,
        val size: Long,
        val dateAdded: Long,
        val thumbnailPath: String? = null,
        val packageName: String? = null  // For hidden apps
    )
    
    data class HiddenApp(
        val packageName: String,
        val appName: String,
        val icon: android.graphics.drawable.Drawable?,
        val dateHidden: Long
    )
    
    enum class VaultItemType {
        IMAGE, VIDEO, DOCUMENT, APK, AUDIO, HIDDEN_APP, OTHER
    }
    
    private val vaultDir: File
        get() = File(context.filesDir, VAULT_DIR).also { 
            if (!it.exists()) it.mkdirs() 
        }
    
    private val thumbnailsDir: File
        get() = File(vaultDir, THUMBNAILS_DIR).also { 
            if (!it.exists()) it.mkdirs() 
        }
    
    /**
     * Get encryption key derived from app-specific data.
     * In production, use EncryptedSharedPreferences or Keystore.
     */
    private fun getEncryptionKey(): ByteArray {
        val keyMaterial = "${context.packageName}_vault_key_v1"
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(keyMaterial.toByteArray())
    }
    
    /**
     * Import a file into the vault with encryption.
     * @param deleteSource If true, attempts to delete the source file after import
     */
    suspend fun importFile(uri: Uri, displayName: String, deleteSource: Boolean = true): Result<Pair<VaultItem, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open file"))
            
            val type = determineFileType(displayName, context.contentResolver.getType(uri))
            val id = generateFileId(displayName)
            val encryptedFile = File(vaultDir, "$id.enc")
            
            // Encrypt and store
            inputStream.use { input ->
                FileOutputStream(encryptedFile).use { output ->
                    encryptFile(input.readBytes(), output)
                }
            }
            
            // Delete source file if requested  
            var sourceDeleted = false
            if (deleteSource) {
                sourceDeleted = try {
                    // First try content resolver delete
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted > 0) {
                        Log.d(TAG, "Source file deleted via ContentResolver: $uri")
                        true
                    } else {
                        // If ContentResolver can't delete, try using DocumentsContract for document URIs
                        if (uri.toString().contains("document")) {
                            try {
                                android.provider.DocumentsContract.deleteDocument(context.contentResolver, uri)
                                Log.d(TAG, "Source file deleted via DocumentsContract: $uri")
                                true
                            } catch (e: Exception) {
                                Log.w(TAG, "DocumentsContract delete failed: ${e.message}")
                                // Try MediaStore delete as last resort
                                attemptMediaStoreDelete(uri)
                            }
                        } else {
                            // Try MediaStore delete for media files
                            attemptMediaStoreDelete(uri)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete source file: ${e.message}")
                    attemptMediaStoreDelete(uri)
                }
            } else {
                true // Not attempting to delete, so consider it "successful"
            }
            
            // Generate thumbnail for images/videos
            val thumbnailPath = if (type == VaultItemType.IMAGE || type == VaultItemType.VIDEO) {
                generateThumbnail(encryptedFile, id, type)
            } else null
            
            val item = VaultItem(
                id = id,
                fileName = displayName,
                type = type,
                size = encryptedFile.length(),
                dateAdded = System.currentTimeMillis(),
                thumbnailPath = thumbnailPath
            )
            
            // Save metadata
            saveMetadata(item)
            
            Log.d(TAG, "File imported to vault: $displayName (source deleted: $sourceDeleted)")
            Result.success(Pair(item, sourceDeleted))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error importing file: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all items in the vault.
     */
    suspend fun getAllItems(): List<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val metadataFile = File(vaultDir, "metadata.txt")
            if (!metadataFile.exists()) return@withContext emptyList()
            
            metadataFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        parseMetadataLine(line)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing metadata: ${e.message}")
                        null
                    }
                }
                .sortedByDescending { it.dateAdded }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading vault items: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Export a file from the vault (decrypt and return as temp file).
     */
    suspend fun exportFile(id: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val encryptedFile = File(vaultDir, "$id.enc")
            if (!encryptedFile.exists()) {
                return@withContext Result.failure(Exception("File not found"))
            }
            
            // Decrypt to temp file
            val tempFile = File(context.cacheDir, "vault_temp_$id")
            FileInputStream(encryptedFile).use { input ->
                FileOutputStream(tempFile).use { output ->
                    decryptFile(input.readBytes(), output)
                }
            }
            
            Result.success(tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting file: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a file from the vault.
     */
    suspend fun deleteFile(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val encryptedFile = File(vaultDir, "$id.enc")
            val thumbnailFile = File(thumbnailsDir, "$id.jpg")
            
            // Securely delete files
            encryptedFile.delete()
            thumbnailFile.delete()
            
            // Remove from metadata
            removeFromMetadata(id)
            
            Log.d(TAG, "File deleted from vault: $id")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get thumbnail bitmap for an item.
     */
    suspend fun getThumbnail(item: VaultItem): Bitmap? = withContext(Dispatchers.IO) {
        try {
            item.thumbnailPath?.let { path ->
                val thumbnailFile = File(path)
                if (thumbnailFile.exists()) {
                    BitmapFactory.decodeFile(thumbnailFile.absolutePath)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading thumbnail: ${e.message}")
            null
        }
    }
    
    /**
     * Get vault statistics.
     */
    suspend fun getVaultStats(): VaultStats = withContext(Dispatchers.IO) {
        val items = getAllItems()
        VaultStats(
            totalItems = items.size,
            totalSize = items.sumOf { it.size },
            imageCount = items.count { it.type == VaultItemType.IMAGE },
            videoCount = items.count { it.type == VaultItemType.VIDEO },
            otherCount = items.count { it.type != VaultItemType.IMAGE && it.type != VaultItemType.VIDEO && it.type != VaultItemType.HIDDEN_APP },
            hiddenAppCount = items.count { it.type == VaultItemType.HIDDEN_APP }
        )
    }
    
    data class VaultStats(
        val totalItems: Int,
        val totalSize: Long,
        val imageCount: Int,
        val videoCount: Int,
        val otherCount: Int,
        val hiddenAppCount: Int
    )
    
    /**
     * Clear entire vault (with secure deletion).
     */
    suspend fun clearVault(): Boolean = withContext(Dispatchers.IO) {
        try {
            vaultDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    // Overwrite before delete
                    try {
                        FileOutputStream(file).use { it.write(ByteArray(file.length().toInt())) }
                    } catch (_: Exception) {}
                    file.delete()
                }
            }
            
            thumbnailsDir.deleteRecursively()
            thumbnailsDir.mkdirs()
            
            Log.d(TAG, "Vault cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing vault: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get list of all installed user apps (excluding system apps).
     */
    suspend fun getInstalledApps(): List<HiddenApp> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
                .filter { appInfo ->
                    // Exclude system apps and our own app
                    (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 &&
                    appInfo.packageName != context.packageName
                }
                .map { appInfo ->
                    HiddenApp(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                        icon = pm.getApplicationIcon(appInfo),
                        dateHidden = 0L
                    )
                }
                .sortedBy { it.appName }
            
            apps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Hide an app by disabling its launcher icon and storing metadata.
     */
    suspend fun hideApp(packageName: String): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val appName = pm.getApplicationLabel(appInfo).toString()
            
            // Disable the app's launcher icon
            val component = pm.getLaunchIntentForPackage(packageName)?.component
            if (component != null) {
                pm.setComponentEnabledSetting(
                    component,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Disabled launcher icon for: $packageName")
            }
            
            // Create vault item for the hidden app
            val id = generateFileId(packageName)
            val item = VaultItem(
                id = id,
                fileName = appName,
                type = VaultItemType.HIDDEN_APP,
                size = 0L,
                dateAdded = System.currentTimeMillis(),
                thumbnailPath = null,
                packageName = packageName
            )
            
            // Save metadata
            saveMetadata(item)
            
            Log.d(TAG, "App hidden: $appName ($packageName)")
            Result.success(item)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding app: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Unhide/restore an app by re-enabling its launcher icon.
     */
    suspend fun unhideApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            
            // Re-enable the app's launcher icon
            val component = pm.getLaunchIntentForPackage(packageName)?.component
            if (component != null) {
                pm.setComponentEnabledSetting(
                    component,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Re-enabled launcher icon for: $packageName")
            }
            
            // Find and remove from metadata
            val items = getAllItems()
            val item = items.find { it.packageName == packageName }
            if (item != null) {
                removeFromMetadata(item.id)
            }
            
            Log.d(TAG, "App unhidden: $packageName")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error unhiding app: ${e.message}", e)
            false
        }
    }
    
    /**
     * Launch a hidden app from within the vault.
     */
    fun launchHiddenApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Launched hidden app: $packageName")
                true
            } else {
                Log.w(TAG, "No launch intent found for: $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}", e)
            false
        }
    }
    
    // ========== Private Helper Methods ==========
    
    private fun encryptFile(data: ByteArray, output: FileOutputStream) {
        val key = SecretKeySpec(getEncryptionKey(), "AES")
        val iv = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        
        // Write IV first
        output.write(iv)
        // Write encrypted data
        output.write(cipher.doFinal(data))
    }
    
    private fun decryptFile(data: ByteArray, output: FileOutputStream) {
        if (data.size < 16) throw Exception("Invalid encrypted file")
        
        val key = SecretKeySpec(getEncryptionKey(), "AES")
        val iv = data.copyOfRange(0, 16)
        val encryptedData = data.copyOfRange(16, data.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        
        output.write(cipher.doFinal(encryptedData))
    }
    
    private fun generateFileId(fileName: String): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..999999).random()
        return "${timestamp}_${random}"
    }
    
    private fun determineFileType(fileName: String, mimeType: String?): VaultItemType {
        return when {
            mimeType?.startsWith("image/") == true -> VaultItemType.IMAGE
            mimeType?.startsWith("video/") == true -> VaultItemType.VIDEO
            mimeType?.startsWith("audio/") == true -> VaultItemType.AUDIO
            mimeType == "application/vnd.android.package-archive" -> VaultItemType.APK
            fileName.endsWith(".apk", true) -> VaultItemType.APK
            fileName.endsWith(".jpg", true) || fileName.endsWith(".png", true) || 
            fileName.endsWith(".jpeg", true) || fileName.endsWith(".gif", true) ||
            fileName.endsWith(".webp", true) -> VaultItemType.IMAGE
            fileName.endsWith(".mp4", true) || fileName.endsWith(".mov", true) || 
            fileName.endsWith(".avi", true) || fileName.endsWith(".mkv", true) ||
            fileName.endsWith(".webm", true) -> VaultItemType.VIDEO
            fileName.endsWith(".mp3", true) || fileName.endsWith(".m4a", true) ||
            fileName.endsWith(".wav", true) || fileName.endsWith(".ogg", true) -> VaultItemType.AUDIO
            fileName.endsWith(".pdf", true) || fileName.endsWith(".doc", true) || 
            fileName.endsWith(".docx", true) || fileName.endsWith(".txt", true) ||
            fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> VaultItemType.DOCUMENT
            else -> VaultItemType.OTHER
        }
    }
    
    private suspend fun generateThumbnail(encryptedFile: File, id: String, type: VaultItemType): String? = withContext(Dispatchers.IO) {
        try {
            // Decrypt to temp file first
            val tempFile = File(context.cacheDir, "temp_$id")
            FileInputStream(encryptedFile).use { input ->
                FileOutputStream(tempFile).use { output ->
                    decryptFile(input.readBytes(), output)
                }
            }
            
            val bitmap = when (type) {
                VaultItemType.IMAGE -> {
                    BitmapFactory.decodeFile(tempFile.absolutePath)
                }
                VaultItemType.VIDEO -> {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(tempFile.absolutePath)
                    retriever.getFrameAtTime(0)?.also {
                        retriever.release()
                    }
                }
                else -> null
            }
            
            tempFile.delete()
            
            bitmap?.let {
                val thumbnail = Bitmap.createScaledBitmap(it, 200, 200, true)
                val thumbnailFile = File(thumbnailsDir, "$id.jpg")
                FileOutputStream(thumbnailFile).use { out ->
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                it.recycle()
                thumbnail.recycle()
                thumbnailFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail: ${e.message}")
            null
        }
    }
    
    private fun saveMetadata(item: VaultItem) {
        val metadataFile = File(vaultDir, "metadata.txt")
        val line = "${item.id}|${item.fileName}|${item.type}|${item.size}|${item.dateAdded}|${item.thumbnailPath ?: ""}|${item.packageName ?: ""}\n"
        metadataFile.appendText(line)
    }
    
    private fun parseMetadataLine(line: String): VaultItem? {
        val parts = line.split("|")
        if (parts.size < 5) return null
        
        return VaultItem(
            id = parts[0],
            fileName = parts[1],
            type = VaultItemType.valueOf(parts[2]),
            size = parts[3].toLong(),
            dateAdded = parts[4].toLong(),
            thumbnailPath = parts.getOrNull(5)?.takeIf { it.isNotBlank() },
            packageName = parts.getOrNull(6)?.takeIf { it.isNotBlank() }
        )
    }
    
    private fun removeFromMetadata(id: String) {
        val metadataFile = File(vaultDir, "metadata.txt")
        if (!metadataFile.exists()) return
        
        val lines = metadataFile.readLines()
            .filter { !it.startsWith("$id|") }
        
        metadataFile.writeText(lines.joinToString("\n"))
    }
    
    private fun attemptMediaStoreDelete(uri: Uri): Boolean {
        return try {
            // For MediaStore URIs (images, videos, audio), try to delete via MediaStore
            val uriString = uri.toString()
            val deleted = when {
                uriString.contains("images") -> {
                    val count = context.contentResolver.delete(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "${android.provider.MediaStore.Images.Media._ID} = ?",
                        arrayOf(android.provider.DocumentsContract.getDocumentId(uri).split(":")[1])
                    )
                    Log.d(TAG, "MediaStore Images delete result: $count for $uri")
                    count > 0
                }
                uriString.contains("video") -> {
                    val count = context.contentResolver.delete(
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        "${android.provider.MediaStore.Video.Media._ID} = ?",
                        arrayOf(android.provider.DocumentsContract.getDocumentId(uri).split(":")[1])
                    )
                    Log.d(TAG, "MediaStore Video delete result: $count for $uri")
                    count > 0
                }
                uriString.contains("audio") -> {
                    val count = context.contentResolver.delete(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        "${android.provider.MediaStore.Audio.Media._ID} = ?",
                        arrayOf(android.provider.DocumentsContract.getDocumentId(uri).split(":")[1])
                    )
                    Log.d(TAG, "MediaStore Audio delete result: $count for $uri")
                    count > 0
                }
                else -> {
                    Log.w(TAG, "Cannot delete file of unknown type: $uri")
                    false
                }
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore delete failed: ${e.message}")
            false
        }
    }
}
