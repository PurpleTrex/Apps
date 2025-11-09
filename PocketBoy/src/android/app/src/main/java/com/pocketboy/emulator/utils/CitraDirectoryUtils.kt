// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.pocketboy.emulator.PocketBoyApplication

object PocketBoyDirectoryUtils {
    const val CITRA_DIRECTORY = "CITRA_DIRECTORY"
    const val LIME3DS_DIRECTORY = "LIME3DS_DIRECTORY"
    val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(PocketBoyApplication.appContext)

    fun needToUpdateManually(): Boolean {
        val directoryString = preferences.getString(CITRA_DIRECTORY, "")
        val limeDirectoryString = preferences.getString(LIME3DS_DIRECTORY,"")
        return (directoryString != "" && limeDirectoryString != "" && directoryString != limeDirectoryString)
    }

    fun attemptAutomaticUpdateDirectory() {
        val directoryString = preferences.getString(CITRA_DIRECTORY, "")
        val limeDirectoryString = preferences.getString(LIME3DS_DIRECTORY,"")
        if (needToUpdateManually()) {
            return;
        }
       if (directoryString == "" && limeDirectoryString != "") {
            // Upgrade from PocketBoy to PocketBoy
           PermissionsHandler.setPocketBoyDirectory(limeDirectoryString)
            removeLimeDirectoryPreference()
            DirectoryInitialization.resetPocketBoyDirectoryState()
            DirectoryInitialization.start()

       } else if (directoryString != "" && directoryString == limeDirectoryString) {
            // Both the PocketBoy and PocketBoy directories are the same,
            // so delete the obsolete PocketBoy value.
            removeLimeDirectoryPreference()
        }
    }

    fun removeLimeDirectoryPreference() {
        preferences.edit().remove(LIME3DS_DIRECTORY).apply()
    }
}
