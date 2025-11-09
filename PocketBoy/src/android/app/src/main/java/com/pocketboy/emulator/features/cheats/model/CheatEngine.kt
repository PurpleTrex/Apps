// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.cheats.model

import androidx.annotation.Keep

@Keep
object CheatEngine {
    external fun loadCheatFile(titleId: Long)
    external fun saveCheatFile(titleId: Long)

    external fun getCheats(): Array<Cheat>

    external fun addCheat(cheat: Cheat?)
    external fun removeCheat(index: Int)
    external fun updateCheat(index: Int, newCheat: Cheat?)
}
