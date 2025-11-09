// Copyright 2023 PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.features.settings.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.pocketboy.emulator.features.settings.model.view.SettingsItem
import com.pocketboy.emulator.features.settings.ui.SettingsAdapter

abstract class SettingViewHolder(itemView: View, protected val adapter: SettingsAdapter) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    init {
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    /**
     * Called by the adapter to set this ViewHolder's child views to display the list item
     * it must now represent.
     *
     * @param item The list item that should be represented by this ViewHolder.
     */
    abstract fun bind(item: SettingsItem)

    /**
     * Called when this ViewHolder's view is clicked on. Implementations should usually pass
     * this event up to the adapter.
     *
     * @param clicked The view that was clicked on.
     */
    abstract override fun onClick(clicked: View)

    abstract override fun onLongClick(clicked: View): Boolean
}
