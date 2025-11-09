// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.ui.viewholder

import android.view.View
import com.pocketboy.emulator.databinding.ListItemSettingsHeaderBinding
import com.pocketboy.emulator.features.settings.model.view.SettingsItem
import com.pocketboy.emulator.features.settings.ui.SettingsAdapter

class HeaderViewHolder(val binding: ListItemSettingsHeaderBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {

    init {
        itemView.setOnClickListener(null)
    }

    override fun bind(item: SettingsItem) {
        binding.textHeaderName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textHeaderDescription.visibility = View.VISIBLE
            binding.textHeaderDescription.setText(item.descriptionId)
        }else {
            binding.textHeaderDescription.visibility = View.GONE
        }
    }

    override fun onClick(clicked: View) {
        // no-op
    }

    override fun onLongClick(clicked: View): Boolean {
        // no-op
        return true
    }
}
