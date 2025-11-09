// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.ui.viewholder

import android.view.View
import com.pocketboy.emulator.databinding.ListItemSettingBinding
import com.pocketboy.emulator.features.settings.model.view.SettingsItem
import com.pocketboy.emulator.features.settings.model.view.StringInputSetting
import com.pocketboy.emulator.features.settings.ui.SettingsAdapter

class StringInputViewHolder(val binding: ListItemSettingBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {
    private lateinit var setting: SettingsItem

    override fun bind(item: SettingsItem) {
        setting = item
        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.visibility = View.VISIBLE
            binding.textSettingDescription.setText(item.descriptionId)
        } else {
            binding.textSettingDescription.visibility = View.GONE
        }
        binding.textSettingValue.visibility = View.VISIBLE
        binding.textSettingValue.text = setting.setting?.valueAsString

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
        if (!setting.isEditable || !setting.isEnabled) {
            adapter.onClickDisabledSetting(!setting.isEditable)
            return
        }
        adapter.onStringInputClick((setting as StringInputSetting), bindingAdapterPosition)
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
