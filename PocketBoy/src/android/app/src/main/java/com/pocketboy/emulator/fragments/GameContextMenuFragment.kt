// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.pocketboy.emulator.R
import com.pocketboy.emulator.activities.CheatsActivity
import com.pocketboy.emulator.model.Game
import com.pocketboy.emulator.utils.Log

/**
 * Context menu dialog for game options
 * Appears when long-pressing on a game tile
 */
class GameContextMenuFragment(
    private val game: Game,
    private val onCheatsClick: () -> Unit = {},
    private val onAchievementsClick: () -> Unit = {},
    private val onMarkCompletedClick: () -> Unit = {},
    private val onViewStatsClick: () -> Unit = {},
    private val onRemoveClick: () -> Unit = {}
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(requireContext().getColor(R.color.pb3ds_surface))

            // Header - Game title
            addView(TextView(requireContext()).apply {
                text = game.title
                textSize = 18f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
                setPadding(24, 24, 24, 12)
            })

            // Divider
            addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2
                )
                setBackgroundColor(requireContext().getColor(R.color.pb3ds_menu_divider))
            })

            // Edit Cheats button
            addView(createMenuItem("Edit Cheats", R.drawable.ic_settings) {
                onCheatsClick()
                openCheatsActivity()
                dismiss()
            })

            // View Achievements button
            addView(createMenuItem("View Achievements", R.drawable.ic_settings) {
                onAchievementsClick()
                dismiss()
                Toast.makeText(requireContext(), "Achievements coming soon", Toast.LENGTH_SHORT).show()
            })

            // Mark as Completed button
            addView(createMenuItem("Mark as Completed", R.drawable.ic_settings) {
                onMarkCompletedClick()
                dismiss()
                Toast.makeText(requireContext(), "Game marked as completed", Toast.LENGTH_SHORT).show()
            })

            // View Statistics button
            addView(createMenuItem("View Statistics", R.drawable.ic_settings) {
                onViewStatsClick()
                dismiss()
                Toast.makeText(requireContext(), "Statistics coming soon", Toast.LENGTH_SHORT).show()
            })

            // Divider
            addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    2
                )
                setBackgroundColor(requireContext().getColor(R.color.pb3ds_menu_divider))
            })

            // Remove from Library button
            addView(createMenuItem("Remove from Library", R.drawable.ic_settings) {
                onRemoveClick()
                dismiss()
                Toast.makeText(requireContext(), "Game removed from library", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun createMenuItem(text: String, iconRes: Int, onClick: () -> Unit): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                56
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setBackgroundColor(android.graphics.Color.TRANSPARENT)

            setOnClickListener { onClick() }

            // Padding
            setPadding(24, 12, 24, 12)

            // Text
            addView(TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                this.text = text
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
            })
        }
    }

    private fun openCheatsActivity() {
        try {
            val intent = Intent(requireContext(), CheatsActivity::class.java).apply {
                putExtra("game", game)
            }
            startActivity(intent)
            Log.info("[GameContextMenuFragment] Opened cheats for game: ${game.title}")
        } catch (e: Exception) {
            Log.error("[GameContextMenuFragment] Error opening cheats: ${e.message}")
            Toast.makeText(requireContext(), "Failed to open cheats", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getTheme(): Int {
        return android.R.style.Theme_Material_Light_NoActionBar
    }

    companion object {
        fun newInstance(
            game: Game,
            onCheatsClick: () -> Unit = {},
            onAchievementsClick: () -> Unit = {},
            onMarkCompletedClick: () -> Unit = {},
            onViewStatsClick: () -> Unit = {},
            onRemoveClick: () -> Unit = {}
        ): GameContextMenuFragment {
            return GameContextMenuFragment(
                game,
                onCheatsClick,
                onAchievementsClick,
                onMarkCompletedClick,
                onViewStatsClick,
                onRemoveClick
            )
        }
    }
}
