// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.pocketboy.emulator.R
import com.pocketboy.emulator.model.Game
import com.pocketboy.emulator.ui.views.GameTileView
import com.pocketboy.emulator.viewmodel.GamesViewModel
import kotlinx.coroutines.launch

/**
 * 3DS-style home fragment displaying games in an interactive grid.
 * Maintains all existing game loading and management functionality.
 */
class ThreeDSHomeFragment : Fragment() {

    private val gamesViewModel: GamesViewModel by viewModels()

    private lateinit var gamesGrid: GridLayout
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var settingsButton: ImageButton
    private lateinit var scrollView: ScrollView

    private val gameTiles = mutableMapOf<String, GameTileView>()
    private var selectedGame: Game? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_3ds_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gamesGrid = view.findViewById(R.id.games_grid)
        loadingSpinner = view.findViewById(R.id.loading_spinner)
        settingsButton = view.findViewById(R.id.settings_button)
        scrollView = view.findViewById(R.id.games_scroll)

        setupUI()
        observeGames()
    }

    private fun setupUI() {
        settingsButton.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun observeGames() {
        lifecycleScope.launch {
            gamesViewModel.gameList.observe(viewLifecycleOwner) { games ->
                if (games.isNotEmpty()) {
                    loadingSpinner.visibility = View.GONE
                    displayGames(games)
                } else {
                    loadingSpinner.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun displayGames(games: List<Game>) {
        gamesGrid.removeAllViews()
        gameTiles.clear()

        for (game in games) {
            val tileView = GameTileView(requireContext()).apply {
                tileTitle = game.title
                tileIcon = null // Will be loaded asynchronously
                accentColor = getAccentColorForGame(game)
                setOnClickListener {
                    selectGame(game)
                    launchGame(game)
                }
            }

            gameTiles[game.filename] = tileView

            val params = GridLayout.LayoutParams().apply {
                width = dpToPx(120)
                height = dpToPx(120)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                setMargins(8, 8, 8, 8)
            }

            gamesGrid.addView(tileView, params)
        }
    }

    private fun getAccentColorForGame(game: Game): Int {
        // Cycle through accent colors based on game position
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#2E7D32"),
            android.graphics.Color.parseColor("#FF6B35"),
            android.graphics.Color.parseColor("#E63946"),
            android.graphics.Color.parseColor("#06A77D"),
            android.graphics.Color.parseColor("#7B68EE"),
            android.graphics.Color.parseColor("#00BCD4")
        )
        return colors[game.hashCode().toInt() % colors.size]
    }

    private fun selectGame(game: Game) {
        selectedGame = game

        // Update visual states
        gameTiles.forEach { (_, tile) ->
            tile.setSelectedState(false, animate = true)
        }
        gameTiles[game.filename]?.setSelectedState(true, animate = true)
    }

    private fun launchGame(game: Game) {
        // Delegate to existing game launch logic
        // This will be handled by the activity's navigation
        parentFragmentManager.primaryNavigationFragment?.let { fragment ->
            if (fragment is GamesFragment) {
                fragment.launchGame(game)
            }
        }
    }

    private fun navigateToSettings() {
        // Navigate to 3DS settings fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ThreeDSSettingsFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    companion object {
        fun newInstance() = ThreeDSHomeFragment()
    }
}
