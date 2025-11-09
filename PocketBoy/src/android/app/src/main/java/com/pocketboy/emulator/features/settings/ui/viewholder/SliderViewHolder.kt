// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.ui.viewholder

import android.view.View
import com.pocketboy.emulator.databinding.ListItemSettingBinding
import com.pocketboy.emulator.features.settings.model.AbstractFloatSetting
import com.pocketboy.emulator.features.settings.model.AbstractIntSetting
import com.pocketboy.emulator.features.settings.model.FloatSetting
import com.pocketboy.emulator.features.settings.model.ScaledFloatSetting
import com.pocketboy.emulator.features.settings.model.view.SettingsItem
import com.pocketboy.emulator.features.settings.model.view.SliderSetting
import com.pocketboy.emulator.features.settings.ui.SettingsAdapter

class SliderViewHolder(val binding: ListItemSettingBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {
    private lateinit var setting: SliderSetting

    override fun bind(item: SettingsItem) {
        setting = item as SliderSetting
        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.visibility = View.VISIBLE
            binding.textSettingDescription.setText(item.descriptionId)
        } else {
            binding.textSettingDescription.visibility = View.GONE
        }
        binding.textSettingValue.visibility = View.VISIBLE
        binding.textSettingValue.text = when (setting.setting) {
            is ScaledFloatSetting ->
                "${(setting.setting as ScaledFloatSetting).float.toInt()}${setting.units}"
            is FloatSetting -> "${(setting.setting as AbstractFloatSetting).float}${setting.units}"
            else -> "${(setting.setting as AbstractIntSetting).int}${setting.units}"
        }

        if (setting.isActive) {
            binding.textSettingName.alpha = 1f
            binding.textSettingDescription.alpha = 1f
            binding.textSettingValue.alpha = 1f
        } else {
            binding.textSettingName.alpha = 0.5f
            binding.textSettingDescription.alpha = 0.5f
            binding.textSettingValue.alpha = 0.5f
        }
    }

    override fun onClick(clicked: View) {
        if (setting.isActive) {
            adapter.onSliderClick(setting, bindingAdapterPosition)
        } else {
            adapter.onClickDisabledSetting(!setting.isEditable)
        }
    }

    override fun onLongClick(clicked: View): Boolean {
        if (setting.isActive) {
            return adapter.onLongClick(setting.setting!!, bindingAdapterPosition)
        } else {
            adapter.onClickDisabledSetting(!setting.isEditable)
        }
        return false
    }
}
