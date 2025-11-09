// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.pocketboy.emulator.R
import com.pocketboy.emulator.features.settings.ui.SettingsActivity
import com.pocketboy.emulator.ui.activities.DualScreenActivity

/**
 * Bottom screen fragment displaying settings menu and game management options.
 */
class ThreeDSBottomScreenFragment : Fragment() {

    private lateinit var btnSystemSettings: Button
    private lateinit var btnInstallCia: Button
    private lateinit var btnSelectGamesFolder: Button
    private lateinit var btnGpuDriver: Button
    private lateinit var btnAbout: Button
    private lateinit var btnShareLog: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_3ds_bottom_screen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSystemSettings = view.findViewById(R.id.btn_system_settings)
        btnInstallCia = view.findViewById(R.id.btn_install_cia)
        btnSelectGamesFolder = view.findViewById(R.id.btn_select_games_folder)
        btnGpuDriver = view.findViewById(R.id.btn_gpu_driver)
        btnAbout = view.findViewById(R.id.btn_about)
        btnShareLog = view.findViewById(R.id.btn_share_log)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        btnSystemSettings.setOnClickListener {
            openSettings()
        }

        btnInstallCia.setOnClickListener {
            installCia()
        }

        btnSelectGamesFolder.setOnClickListener {
            selectGamesFolder()
        }

        btnGpuDriver.setOnClickListener {
            manageGpuDriver()
        }

        btnAbout.setOnClickListener {
            openAbout()
        }

        btnShareLog.setOnClickListener {
            shareLog()
        }
    }

    private fun openSettings() {
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    private fun installCia() {
        // TODO: Implement CIA installation dialog
        // This should open a file picker and install the CIA file
    }

    private fun selectGamesFolder() {
        // TODO: Implement folder selection dialog
        // This should open a folder picker to select games directory
    }

    private fun manageGpuDriver() {
        // TODO: Implement GPU driver manager
        // This should show available GPU drivers and allow installation
    }

    private fun openAbout() {
        // Navigate to about fragment
        parentFragmentManager.beginTransaction()
            .replace(
                (requireActivity() as? DualScreenActivity)?.let { R.id.bottom_screen_container } ?: R.id.bottom_screen_container,
                AboutFragment()
            )
            .addToBackStack(null)
            .commit()
    }

    private fun shareLog() {
        // TODO: Implement log sharing
        // This should share the emulator log file via Intent
    }

    companion object {
        fun newInstance() = ThreeDSBottomScreenFragment()
    }
}
