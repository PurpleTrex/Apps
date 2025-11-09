// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.utils

import androidx.annotation.Keep
import androidx.lifecycle.ViewModelProvider
import com.pocketboy.emulator.NativeLibrary
import com.pocketboy.emulator.R
import com.pocketboy.emulator.activities.EmulationActivity
import com.pocketboy.emulator.viewmodel.EmulationViewModel

@Keep
object DiskShaderCacheProgress {
    private lateinit var emulationViewModel: EmulationViewModel

    private fun prepareViewModel() {
        emulationViewModel =
            ViewModelProvider(
                NativeLibrary.sEmulationActivity.get() as EmulationActivity
            )[EmulationViewModel::class.java]
    }

    @JvmStatic
    fun loadProgress(stage: LoadCallbackStage, progress: Int, max: Int) {
        val emulationActivity = NativeLibrary.sEmulationActivity.get()
        if (emulationActivity == null) {
            Log.error("[DiskShaderCacheProgress] EmulationActivity not present")
            return
        }

        emulationActivity.runOnUiThread {
            when (stage) {
                LoadCallbackStage.Prepare -> prepareViewModel()
                LoadCallbackStage.Decompile -> emulationViewModel.updateProgress(
                    emulationActivity.getString(R.string.preparing_shaders),
                    progress,
                    max
                )

                LoadCallbackStage.Build -> emulationViewModel.updateProgress(
                    emulationActivity.getString(R.string.building_shaders),
                    progress,
                    max
                )

                LoadCallbackStage.Complete -> emulationActivity.onEmulationStarted()
            }
        }
    }

    // Equivalent to VideoCore::LoadCallbackStage
    enum class LoadCallbackStage {
        Prepare,
        Decompile,
        Build,
        Complete
    }
}
