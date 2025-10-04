package com.example.notepad.security

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import kotlin.random.Random

/**
 * AppDestructionManager handles complete app data destruction and self-uninstall.
 * 
 * This is triggered by a 3-second long press on the main screen and will:
 * 1. Corrupt all database files with random data
 * 2. Overwrite all files with random bytes multiple times
 * 3. Delete all caches, preferences, and files
 * 4. Clear all app data
 * 5. Attempt to uninstall the app
 * 
 * WARNING: This is irreversible and will destroy ALL user data!
 */
object AppDestructionManager {
    private const val TAG = "AppDestruction"
    private const val OVERWRITE_PASSES = 3 // Number of times to overwrite files
    
    /**
     * Execute complete app destruction sequence.
     * This will wipe all data and attempt automatic self-uninstall with no user prompt.
     */
    suspend fun executeDestruction(context: Context) = withContext(Dispatchers.IO) {
        Log.w(TAG, "!!! EXECUTING COMPLETE APP DESTRUCTION !!!")
        
        try {
            // Step 1: Corrupt all databases
            corruptDatabases(context)
            
            // Step 2: Overwrite all app files
            overwriteAllFiles(context)
            
            // Step 3: Clear all caches
            clearAllCaches(context)
            
            // Step 4: Clear all preferences
            clearAllPreferences(context)
            
            // Step 5: Delete all external files
            clearExternalStorage(context)
            
            // Step 6: Clear WebView data
            clearWebViewData(context)
            
            // Step 7: Attempt automatic uninstall (no user prompt)
            withContext(Dispatchers.Main) {
                uninstallApp(context)
            }
            
            // Step 8: Small delay then force kill the app
            delay(500)
            
            Log.w(TAG, "Destruction sequence completed - terminating app")
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during destruction: ${e.message}", e)
            // Force kill anyway as last resort
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)
        }
    }
    
    /**
     * Corrupt all SQLite database files with random data.
     */
    private fun corruptDatabases(context: Context) {
        try {
            val dbDir = context.getDatabasePath("dummy").parentFile ?: return
            
            dbDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".db") || 
                    file.name.endsWith(".db-shm") || 
                    file.name.endsWith(".db-wal")) {
                    
                    Log.d(TAG, "Corrupting database: ${file.name}")
                    overwriteFile(file, OVERWRITE_PASSES)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error corrupting databases: ${e.message}")
        }
    }
    
    /**
     * Overwrite all app files with random data.
     */
    private fun overwriteAllFiles(context: Context) {
        try {
            // Overwrite files directory
            context.filesDir?.let { overwriteDirectory(it) }
            
            // Overwrite code cache
            context.codeCacheDir?.let { overwriteDirectory(it) }
            
            // Overwrite no backup files
            context.noBackupFilesDir?.let { overwriteDirectory(it) }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error overwriting files: ${e.message}")
        }
    }
    
    /**
     * Recursively overwrite all files in a directory.
     */
    private fun overwriteDirectory(dir: File) {
        if (!dir.exists() || !dir.isDirectory) return
        
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                overwriteDirectory(file)
            } else {
                overwriteFile(file, OVERWRITE_PASSES)
            }
        }
        
        // Delete the directory after overwriting
        dir.deleteRecursively()
    }
    
    /**
     * Overwrite a file with random data multiple times.
     * Uses DoD 5220.22-M standard (3 passes minimum).
     */
    private fun overwriteFile(file: File, passes: Int = 3) {
        try {
            if (!file.exists() || !file.canWrite()) return
            
            val fileSize = file.length()
            if (fileSize <= 0) {
                file.delete()
                return
            }
            
            RandomAccessFile(file, "rw").use { raf ->
                // Pass 1: Write random data
                val randomData = ByteArray(minOf(fileSize.toInt(), 8192))
                Random.nextBytes(randomData)
                
                for (pass in 1..passes) {
                    raf.seek(0)
                    var remaining = fileSize
                    
                    while (remaining > 0) {
                        val toWrite = minOf(remaining, randomData.size.toLong()).toInt()
                        
                        // Change the pattern each pass
                        when (pass % 3) {
                            0 -> randomData.fill(0xFF.toByte()) // All 1s
                            1 -> randomData.fill(0x00.toByte()) // All 0s
                            2 -> Random.nextBytes(randomData)    // Random
                        }
                        
                        raf.write(randomData, 0, toWrite)
                        remaining -= toWrite
                    }
                    
                    // Force sync to disk
                    raf.fd.sync()
                }
            }
            
            // Delete the file after overwriting
            file.delete()
            Log.d(TAG, "Securely deleted: ${file.name}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error overwriting file ${file.name}: ${e.message}")
            // Try simple delete as fallback
            try { file.delete() } catch (_: Exception) {}
        }
    }
    
    /**
     * Clear all cache directories.
     */
    private fun clearAllCaches(context: Context) {
        try {
            // Internal cache
            context.cacheDir?.deleteRecursively()
            
            // External cache
            context.externalCacheDir?.deleteRecursively()
            
            // Code cache
            context.codeCacheDir?.deleteRecursively()
            
            Log.d(TAG, "All caches cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing caches: ${e.message}")
        }
    }
    
    /**
     * Clear all SharedPreferences.
     */
    private fun clearAllPreferences(context: Context) {
        try {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            
            prefsDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".xml")) {
                    overwriteFile(file, OVERWRITE_PASSES)
                    
                    // Also clear from memory
                    val prefsName = file.nameWithoutExtension
                    context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .commit()
                }
            }
            
            prefsDir.deleteRecursively()
            Log.d(TAG, "All preferences cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preferences: ${e.message}")
        }
    }
    
    /**
     * Clear external storage files.
     */
    private fun clearExternalStorage(context: Context) {
        try {
            context.getExternalFilesDir(null)?.deleteRecursively()
            context.externalMediaDirs?.forEach { it.deleteRecursively() }
            
            Log.d(TAG, "External storage cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing external storage: ${e.message}")
        }
    }
    
    /**
     * Clear WebView data.
     */
    private fun clearWebViewData(context: Context) {
        try {
            android.webkit.WebStorage.getInstance().deleteAllData()
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()
            
            context.deleteDatabase("webview.db")
            context.deleteDatabase("webviewCache.db")
            
            Log.d(TAG, "WebView data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing WebView data: ${e.message}")
        }
    }
    
    /**
     * Attempt to uninstall the app automatically.
     * Uses REQUEST_DELETE_PACKAGES permission for minimal user interaction.
     */
    private fun uninstallApp(context: Context) {
        try {
            val packageName = context.packageName
            
            // Use REQUEST_DELETE_PACKAGES intent for automatic uninstall (Android 10+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val packageInstaller = context.packageManager.packageInstaller
                val intent = Intent(context, context::class.java)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
                
                val pendingIntent = android.app.PendingIntent.getActivity(
                    context, 
                    0, 
                    intent, 
                    android.app.PendingIntent.FLAG_IMMUTABLE
                )
                
                packageInstaller.uninstall(packageName, pendingIntent.intentSender)
                Log.w(TAG, "Automatic uninstall initiated for: $packageName")
            } else {
                // Fallback for older Android versions
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:$packageName")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Log.w(TAG, "Uninstall intent launched for: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching uninstall: ${e.message}")
            
            // Last resort: Try to disable the app completely
            try {
                val pm = context.packageManager
                pm.setApplicationEnabledSetting(
                    context.packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                
                // Also kill the app process
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(0)
            } catch (e2: Exception) {
                Log.e(TAG, "Error disabling app: ${e2.message}")
                // Force kill anyway
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(0)
            }
        }
    }
    
    /**
     * Quick emergency wipe (less thorough but faster).
     * Use this if speed is critical.
     */
    suspend fun quickWipe(context: Context) = withContext(Dispatchers.IO) {
        try {
            // Just delete everything without overwriting
            context.filesDir?.deleteRecursively()
            context.cacheDir?.deleteRecursively()
            context.codeCacheDir?.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
            context.getExternalFilesDir(null)?.deleteRecursively()
            
            val dbDir = context.getDatabasePath("dummy").parentFile
            dbDir?.deleteRecursively()
            
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            prefsDir.deleteRecursively()
            
            clearWebViewData(context)
            uninstallApp(context)
            
            Log.w(TAG, "Quick wipe completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during quick wipe: ${e.message}")
        }
    }
}
