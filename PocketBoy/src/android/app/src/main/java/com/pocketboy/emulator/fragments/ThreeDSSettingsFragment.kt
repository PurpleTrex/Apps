// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pocketboy.emulator.R
import com.pocketboy.emulator.features.settings.model.SettingsViewModel
import com.pocketboy.emulator.features.settings.model.view.SettingsItem

/**
 * 3DS-style settings fragment displaying all emulator settings
 * in a vertical menu format similar to the 3DS System Settings app.
 */
class ThreeDSSettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var settingsContainer: LinearLayout
    private lateinit var backButton: ImageButton
    private lateinit var applyButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_3ds_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsContainer = view.findViewById(R.id.settings_categories)
        backButton = view.findViewById(R.id.settings_back_button)
        applyButton = view.findViewById(R.id.settings_apply_button)
        cancelButton = view.findViewById(R.id.settings_cancel_button)

        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        applyButton.setOnClickListener {
            applySettings()
            parentFragmentManager.popBackStack()
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadSettings() {
        // Load all settings from ViewModel
        val settings = settingsViewModel.settingsCategories

        // Organize settings by category
        val groupedSettings = settings.groupBy { it.javaClass.simpleName }

        for ((category, categoryItems) in groupedSettings) {
            addCategoryHeader(category)

            for (setting in categoryItems) {
                addSettingItem(setting)
            }

            addDivider()
        }
    }

    private fun addCategoryHeader(categoryName: String) {
        val headerView = TextView(requireContext()).apply {
            text = categoryName
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            setPadding(20, 16, 20, 8)
            setBackgroundColor(android.graphics.Color.parseColor("#0F3460"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        settingsContainer.addView(headerView)
    }

    private fun addSettingItem(setting: SettingsItem) {
        val itemView = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                56
            )
            background = android.graphics.drawable.ColorDrawable(
                android.graphics.Color.parseColor("#162342")
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 0, 20, 0)

            // Setting title
            val titleView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                text = setting.name
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
            }
            addView(titleView)

            // Value indicator
            val valueView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = getSettingValue(setting)
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#B0B0B0"))
                setPadding(8, 0, 0, 0)
            }
            addView(valueView)

            // Arrow indicator
            val arrowView = android.widget.ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    24,
                    24
                )
                (layoutParams as LinearLayout.LayoutParams).marginStart = 16
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(
                    android.graphics.Color.parseColor("#B0B0B0"),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            addView(arrowView)

            // Click listener to open setting details
            setOnClickListener {
                openSettingDetail(setting)
            }
        }

        settingsContainer.addView(itemView)
    }

    private fun addDivider() {
        val divider = View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(android.graphics.Color.parseColor("#2A3F5F"))
        }
        settingsContainer.addView(divider)
    }

    private fun getSettingValue(setting: SettingsItem): String {
        // Return the current value of the setting
        // This is simplified; actual implementation depends on setting type
        return "..."
    }

    private fun openSettingDetail(setting: SettingsItem) {
        // Open a detail view for this specific setting
        // This would show different UI based on setting type
        // (toggle, slider, dropdown, text input, etc.)
    }

    private fun applySettings() {
        // Apply all changed settings
        settingsViewModel.saveSettings()
    }

    companion object {
        fun newInstance() = ThreeDSSettingsFragment()
    }
}
