// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.model.view

import com.pocketboy.emulator.features.settings.model.AbstractFloatSetting
import com.pocketboy.emulator.features.settings.model.AbstractIntSetting
import com.pocketboy.emulator.features.settings.model.AbstractSetting
import com.pocketboy.emulator.features.settings.model.FloatSetting
import com.pocketboy.emulator.features.settings.model.ScaledFloatSetting
import com.pocketboy.emulator.utils.Log

class SliderSetting(
    setting: AbstractSetting?,
    titleId: Int,
    descriptionId: Int,
    val min: Int,
    val max: Int,
    val units: String,
    val key: String? = null,
    val defaultValue: Float? = null,
    override var isEnabled: Boolean = true
) : SettingsItem(setting, titleId, descriptionId) {
    override val type = TYPE_SLIDER
    val selectedFloat: Float
        get() {
            val setting = setting ?: return defaultValue!!.toFloat()

            val ret = when (setting) {
                is AbstractIntSetting -> setting.int.toFloat()
                is FloatSetting -> setting.float
                is ScaledFloatSetting -> setting.float
                else -> {
                    Log.error("[SliderSetting] Error casting setting type.")
                    -1f
                }
            }
            return ret.coerceIn(min.toFloat(), max.toFloat())
        }
    /**
     * Write a value to the backing int. If that int was previously null,
     * initializes a new one and returns it, so it can be added to the Hashmap.
     *
     * @param selection New value of the int.
     * @return the existing setting with the new value applied.
     */
    fun setSelectedValue(selection: Int): AbstractIntSetting {
        val intSetting = setting as AbstractIntSetting
        intSetting.int = selection
        return intSetting
    }

    /**
     * Write a value to the backing float. If that float was previously null,
     * initializes a new one and returns it, so it can be added to the Hashmap.
     *
     * @param selection New value of the float.
     * @return the existing setting with the new value applied.
     */
    fun setSelectedValue(selection: Float): AbstractFloatSetting {
        val floatSetting = setting as AbstractFloatSetting
        if (floatSetting is ScaledFloatSetting) {
            floatSetting.float = selection
        } else {
            floatSetting.float = selection
        }
        return floatSetting
    }
}
