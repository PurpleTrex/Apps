// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pocketboy.emulator.R
import com.pocketboy.emulator.ui.main.MainActivity
import com.pocketboy.emulator.viewmodel.HomeViewModel

class SelectUserDirectoryDialogFragment : DialogFragment() {
    private lateinit var mainActivity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mainActivity = requireActivity() as MainActivity

        isCancelable = false

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_pocketboy_user_folder)
            .setMessage(R.string.selecting_user_directory_without_write_permissions)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                mainActivity?.openPocketBoyDirectoryLostPermission?.launch(null)
            }
            .show()
    }

    companion object {
        const val TAG = "SelectUserDirectoryDialogFragment"

        fun newInstance(activity: FragmentActivity): SelectUserDirectoryDialogFragment {
            ViewModelProvider(activity)[HomeViewModel::class.java].setPickingUserDir(true)
            return SelectUserDirectoryDialogFragment()
        }
    }
}
