// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.pocketboy.emulator.PocketBoyApplication
import com.pocketboy.emulator.R
import com.pocketboy.emulator.features.settings.ui.SettingsActivity
import com.pocketboy.emulator.features.settings.model.SettingsViewModel
import com.pocketboy.emulator.ui.activities.DualScreenActivity
import com.pocketboy.emulator.viewmodel.GamesViewModel
import com.pocketboy.emulator.utils.GameHelper
import com.pocketboy.emulator.utils.Log

/**
 * Bottom screen fragment displaying settings menu and game management options.
 */
class ThreeDSBottomScreenFragment : Fragment() {

    // ViewModels - shared with activity
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val gamesViewModel: GamesViewModel by activityViewModels()

    private lateinit var btnProfileStats: Button
    private lateinit var btnSystemSettings: Button
    private lateinit var btnInstallCia: Button
    private lateinit var btnSelectGamesFolder: Button
    private lateinit var btnGpuDriver: Button
    private lateinit var btnAbout: Button
    private lateinit var btnShareLog: Button

    // Activity result launchers
    private val selectGamesFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            saveGamesFolderPath(uri)
        }
    }

    private val selectCiaFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                installCiaFile(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_3ds_bottom_screen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnProfileStats = view.findViewById(R.id.btn_profile_stats)
        btnSystemSettings = view.findViewById(R.id.btn_system_settings)
        btnInstallCia = view.findViewById(R.id.btn_install_cia)
        btnSelectGamesFolder = view.findViewById(R.id.btn_select_games_folder)
        btnGpuDriver = view.findViewById(R.id.btn_gpu_driver)
        btnAbout = view.findViewById(R.id.btn_about)
        btnShareLog = view.findViewById(R.id.btn_share_log)

        setupEventListeners()
    }

    private fun setupEventListeners() {
        btnProfileStats.setOnClickListener {
            openProfileStats()
        }

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

    private fun openProfileStats() {
        try {
            // Navigate to ProfileStatsFragment within the bottom screen
            parentFragmentManager.beginTransaction()
                .replace(
                    (requireActivity() as? DualScreenActivity)?.let { R.id.bottom_screen_container } ?: R.id.bottom_screen_container,
                    ProfileStatsFragment()
                )
                .addToBackStack(null)
                .commit()
            Log.info("[ThreeDSBottomScreenFragment] ProfileStatsFragment opened")
        } catch (e: Exception) {
            Log.error("[ThreeDSBottomScreenFragment] Failed to open profile stats: ${e.message}")
            Toast.makeText(
                requireContext(),
                "Failed to open profile",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openSettings() {
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    private fun installCia() {
        // Open file picker for CIA files
        selectCiaFileLauncher.launch(arrayOf("application/octet-stream"))
    }

    private fun installCiaFile(uri: Uri) {
        // Install the CIA file using NativeLibrary
        try {
            val filePath = uri.toString()
            Toast.makeText(
                requireContext(),
                "Installing CIA file: ${uri.lastPathSegment}",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Call native method to install CIA when available
            // For now, just show confirmation
            Log.info("[ThreeDSBottomScreenFragment] CIA install requested for: $filePath")
        } catch (e: Exception) {
            Log.error("[ThreeDSBottomScreenFragment] Failed to install CIA: ${e.message}")
            Toast.makeText(
                requireContext(),
                "Failed to install CIA file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun selectGamesFolder() {
        // Open folder picker for games directory
        selectGamesFolderLauncher.launch(null)
    }

    private fun saveGamesFolderPath(uri: Uri) {
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            preferences.edit()
                .putString(GameHelper.KEY_GAME_PATH, uri.toString())
                .apply()

            Toast.makeText(
                requireContext(),
                "Games folder updated successfully",
                Toast.LENGTH_SHORT
            ).show()

            // Reload games from the new path
            gamesViewModel.reloadGames(directoryChanged = true)
            Log.info("[ThreeDSBottomScreenFragment] Games path updated to: ${uri.toString()}")
        } catch (e: Exception) {
            Log.error("[ThreeDSBottomScreenFragment] Failed to save games path: ${e.message}")
            Toast.makeText(
                requireContext(),
                "Failed to update games folder",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun manageGpuDriver() {
        // For now, show a toast indicating GPU driver management
        // TODO: Implement actual GPU driver selection/installation UI
        Toast.makeText(
            requireContext(),
            "GPU driver management coming soon",
            Toast.LENGTH_SHORT
        ).show()
        Log.info("[ThreeDSBottomScreenFragment] GPU driver manager requested")
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
        // Share log via email or other sharing methods
        try {
            val logContent = "PocketBoy Emulator Log\n\nPlease attach emulator logs to your issue.\n\n" +
                "Device Information:\n" +
                "Android Version: ${android.os.Build.VERSION.RELEASE}\n" +
                "Device Model: ${android.os.Build.MODEL}\n" +
                "PocketBoy Version: ${requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName}"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "PocketBoy Emulator Log")
                putExtra(Intent.EXTRA_TEXT, logContent)
            }

            startActivity(Intent.createChooser(shareIntent, "Share log via"))
            Log.info("[ThreeDSBottomScreenFragment] Log sharing initiated")
        } catch (e: Exception) {
            Log.error("[ThreeDSBottomScreenFragment] Failed to share log: ${e.message}")
            Toast.makeText(
                requireContext(),
                "Failed to share log",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        fun newInstance() = ThreeDSBottomScreenFragment()
    }
}
