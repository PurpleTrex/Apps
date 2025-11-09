// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.utils

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.pocketboy.emulator.fragments.PocketBoyDirectoryDialogFragment
import com.pocketboy.emulator.fragments.CopyDirProgressDialog
import com.pocketboy.emulator.model.SetupCallback
import com.pocketboy.emulator.viewmodel.HomeViewModel

/**
 * PocketBoy directory initialization ui flow controller.
 */
class PocketBoyDirectoryHelper(private val fragmentActivity: FragmentActivity, private val lostPermission: Boolean) {
    fun showPocketBoyDirectoryDialog(result: Uri, callback: SetupCallback? = null, buttonState: () -> Unit) {
        val pocketboyDirectoryDialog = PocketBoyDirectoryDialogFragment.newInstance(
            fragmentActivity,
            result.toString(),
            PocketBoyDirectoryDialogFragment.Listener { moveData: Boolean, path: Uri ->
                val previous = PermissionsHandler.pocketboyDirectory
                // Do noting if user select the previous path.
                if (path == previous && !lostPermission) {
                    return@Listener
                }

                val takeFlags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                fragmentActivity.contentResolver.takePersistableUriPermission(
                    path,
                    takeFlags
                )
                if (!moveData || previous.toString().isEmpty()) {
                    initializePocketBoyDirectory(path)
                    buttonState()
                    val viewModel = ViewModelProvider(fragmentActivity)[HomeViewModel::class.java]
                    viewModel.setUserDir(fragmentActivity, path.path!!)
                    viewModel.setPickingUserDir(false)
                    return@Listener
                }

                // If user check move data, show copy progress dialog.
                CopyDirProgressDialog.newInstance(fragmentActivity, previous, path, callback)
                    ?.show(fragmentActivity.supportFragmentManager, CopyDirProgressDialog.TAG)
            })
        pocketboyDirectoryDialog.show(
            fragmentActivity.supportFragmentManager,
            PocketBoyDirectoryDialogFragment.TAG
        )
    }

    companion object {
        fun initializePocketBoyDirectory(path: Uri) {
            PermissionsHandler.setPocketBoyDirectory(path.toString())
            DirectoryInitialization.resetPocketBoyDirectoryState()
            DirectoryInitialization.start()
        }
    }
}
