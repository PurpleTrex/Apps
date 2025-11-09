// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeSetting(
    val titleId: Int,
    val descriptionId: Int,
    val iconId: Int,
    val onClick: () -> Unit,
    val isEnabled: () -> Boolean = { true },
    val disabledTitleId: Int = 0,
    val disabledMessageId: Int = 0,
    val details: StateFlow<String> = MutableStateFlow("")
)
