// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.model.view

import com.pocketboy.emulator.features.settings.model.AbstractSetting
import com.pocketboy.emulator.features.settings.model.AbstractStringSetting

class StringInputSetting(
    setting: AbstractSetting?,
    titleId: Int,
    descriptionId: Int,
    val defaultValue: String,
    val characterLimit: Int = 0,
    override var isEnabled: Boolean = true
) : SettingsItem(setting, titleId, descriptionId) {
    override val type = TYPE_STRING_INPUT

    val selectedValue: String
        get() = setting?.valueAsString ?: defaultValue

    fun setSelectedValue(selection: String): AbstractStringSetting {
        val stringSetting = setting as AbstractStringSetting
        stringSetting.string = selection
        return stringSetting
    }
}
