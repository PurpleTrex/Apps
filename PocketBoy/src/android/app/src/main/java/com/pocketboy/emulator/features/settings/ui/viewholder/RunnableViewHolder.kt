// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.ui.viewholder

import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.pocketboy.emulator.NativeLibrary
import com.pocketboy.emulator.activities.EmulationActivity
import com.pocketboy.emulator.databinding.ListItemSettingBinding
import com.pocketboy.emulator.features.settings.model.view.RunnableSetting
import com.pocketboy.emulator.features.settings.model.view.SettingsItem
import com.pocketboy.emulator.features.settings.ui.SettingsAdapter

class RunnableViewHolder(val binding: ListItemSettingBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {
    private lateinit var setting: RunnableSetting

    override fun bind(item: SettingsItem) {
        setting = item as RunnableSetting
        if (item.iconId == 0) {
            binding.icon.visibility = View.GONE
        } else {
            binding.icon.visibility = View.VISIBLE
            binding.icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    binding.icon.resources,
                    item.iconId,
                    binding.icon.context.theme
                )
            )
        }

        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.setText(item.descriptionId)
            binding.textSettingDescription.visibility = View.VISIBLE
        } else {
            binding.textSettingDescription.visibility = View.GONE
        }

        if (setting.value != null) {
            binding.textSettingValue.visibility = View.VISIBLE
            binding.textSettingValue.text = setting.value!!.invoke()
        } else {
            binding.textSettingValue.visibility = View.GONE
        }

        if (setting.isEditable) {
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
        if (!setting.isRuntimeRunnable && EmulationActivity.isRunning()) {
            adapter.onClickDisabledSetting(true)
        } else {
            setting.runnable.invoke()
        }
    }

    override fun onLongClick(clicked: View): Boolean {
        // no-op
        return true
    }
}
