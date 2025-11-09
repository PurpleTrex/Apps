// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.model.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class SubmenuSetting(
    @StringRes titleId: Int,
    @StringRes descriptionId: Int,
    @DrawableRes val iconId: Int,
    val menuKey: String
) : SettingsItem(null, titleId, descriptionId) {
    override val type = TYPE_SUBMENU
}
