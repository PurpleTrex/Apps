// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.ui.viewholder

import android.view.View
import android.widget.CompoundButton
import com.pocketboy.emulator.databinding.ListItemSettingSwitchBinding
import com.pocketboy.emulator.features.settings.model.view.SettingsItem
import com.pocketboy.emulator.features.settings.model.view.SwitchSetting
import com.pocketboy.emulator.features.settings.ui.SettingsAdapter

class SwitchSettingViewHolder(val binding: ListItemSettingSwitchBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {

    private lateinit var setting: SwitchSetting

    override fun bind(item: SettingsItem) {
        setting = item as SwitchSetting
        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.setText(item.descriptionId)
            binding.textSettingDescription.visibility = View.VISIBLE
        } else {
            binding.textSettingDescription.text = ""
            binding.textSettingDescription.visibility = View.GONE
        }

        binding.switchWidget.setOnCheckedChangeListener(null)
        binding.switchWidget.isChecked = setting.isChecked
        binding.switchWidget.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            adapter.onBooleanClick(item, bindingAdapterPosition, binding.switchWidget.isChecked)
        }

        binding.switchWidget.isEnabled = setting.isActive

        val textAlpha = if (setting.isActive) 1f else 0.5f
        binding.textSettingName.alpha = textAlpha
        binding.textSettingDescription.alpha = textAlpha
    }

    override fun onClick(clicked: View) {
        if (setting.isActive) {
            binding.switchWidget.toggle()
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
