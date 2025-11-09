// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import androidx.documentfile.provider.DocumentFile
import com.pocketboy.emulator.PocketBoyApplication

object PermissionsHandler {
    const val CITRA_DIRECTORY = "CITRA_DIRECTORY"
    val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(PocketBoyApplication.appContext)

    fun hasWriteAccess(context: Context): Boolean {
        try {
            if (pocketboyDirectory.toString().isEmpty()) {
                return false
            }

            val uri = pocketboyDirectory
            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            val root = DocumentFile.fromTreeUri(context, uri)
            if (root != null && root.exists()) {
                return true
            }

            context.contentResolver.releasePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            // Do not use native library logging, as the native library may not be loaded yet
            android.util.Log.e("PermissionsHandler", "Cannot check pocketboy data directory permission, error: ${e.message}")
        }
        return false
    }

    val pocketboyDirectory: Uri
        get() {
            val directoryString = preferences.getString(CITRA_DIRECTORY, "")
            return Uri.parse(directoryString)
        }

    fun setPocketBoyDirectory(uriString: String?) =
        preferences.edit().putString(CITRA_DIRECTORY, uriString).apply()
}
