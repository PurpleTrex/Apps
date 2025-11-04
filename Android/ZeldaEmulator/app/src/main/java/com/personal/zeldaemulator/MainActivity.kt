package com.personal.zeldaemulator

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var emulatorView: EmulatorView
    private lateinit var touchControls: TouchControlsOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide system UI for immersive fullscreen experience
        hideSystemUI()

        // Keep screen on during gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        emulatorView = findViewById(R.id.emulatorView)
        touchControls = findViewById(R.id.touchControls)

        // Copy ROM from assets to internal storage
        val romFile = copyRomFromAssets()

        // Connect touch controls to emulator
        touchControls.setInputListener(emulatorView)

        // Load and start emulation
        emulatorView.loadRom(romFile)

        // Try to load saved state if it exists
        val saveFile = File(filesDir, "zelda_autosave.state")
        if (saveFile.exists()) {
            emulatorView.loadState(saveFile)
        }

        emulatorView.start()
    }

    private fun copyRomFromAssets(): File {
        val romFile = File(filesDir, "zelda.nes")

        if (!romFile.exists()) {
            try {
                assets.open("zelda.nes").use { input ->
                    FileOutputStream(romFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                // If ROM file doesn't exist in assets, create a placeholder
                // In production, you would handle this error appropriately
                e.printStackTrace()
            }
        }

        return romFile
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        emulatorView.resume()
    }

    override fun onPause() {
        super.onPause()
        emulatorView.pause()

        // Auto-save state when pausing
        val saveFile = File(filesDir, "zelda_autosave.state")
        emulatorView.saveState(saveFile)
    }

    override fun onDestroy() {
        super.onDestroy()
        emulatorView.stop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}
