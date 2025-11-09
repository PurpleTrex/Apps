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
import android.widget.LinearLayout
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
 * Top screen fragment displaying game tiles in a 3-column grid.
 */
class ThreeDSTopScreenFragment : Fragment() {

    private val gamesViewModel: GamesViewModel by viewModels()

    private lateinit var gamesGrid: GridLayout
    private lateinit var gamesScroll: ScrollView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnSearch: ImageButton
    private lateinit var btnFavorites: ImageButton
    private lateinit var btnRecent: ImageButton
    private lateinit var btnInfo: ImageButton

    private val gameTiles = mutableMapOf<String, GameTileView>()
    private var selectedGame: Game? = null
    private var filterMode = FilterMode.ALL

    enum class FilterMode {
        ALL, FAVORITES, RECENT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_3ds_top_screen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gamesGrid = view.findViewById(R.id.games_grid)
        gamesScroll = view.findViewById(R.id.games_scroll)
        emptyState = view.findViewById(R.id.empty_state)
        btnRefresh = view.findViewById(R.id.btn_refresh)
        btnSearch = view.findViewById(R.id.btn_search)
        btnFavorites = view.findViewById(R.id.btn_favorites)
        btnRecent = view.findViewById(R.id.btn_recent)
        btnInfo = view.findViewById(R.id.btn_info)

        setupUI()
        observeGames()
    }

    private fun setupUI() {
        btnRefresh.setOnClickListener {
            loadGames()
        }

        btnSearch.setOnClickListener {
            // TODO: Open search interface
        }

        btnFavorites.setOnClickListener {
            filterMode = FilterMode.FAVORITES
            loadGames()
        }

        btnRecent.setOnClickListener {
            filterMode = FilterMode.RECENT
            loadGames()
        }

        btnInfo.setOnClickListener {
            // TODO: Open game info/about
        }
    }

    private fun observeGames() {
        lifecycleScope.launch {
            gamesViewModel.gameList.observe(viewLifecycleOwner) { games ->
                displayGames(applyFilter(games))
            }
        }
    }

    private fun loadGames() {
        gamesViewModel.reloadGameList()
    }

    private fun applyFilter(games: List<Game>): List<Game> {
        return when (filterMode) {
            FilterMode.FAVORITES -> games.filter { it.isFavorite }
            FilterMode.RECENT -> games.take(6) // Show only 6 most recent
            FilterMode.ALL -> games
        }
    }

    private fun displayGames(games: List<Game>) {
        gamesGrid.removeAllViews()
        gameTiles.clear()

        if (games.isEmpty()) {
            gamesScroll.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            return
        }

        gamesScroll.visibility = View.VISIBLE
        emptyState.visibility = View.GONE

        for ((index, game) in games.withIndex()) {
            val tileView = GameTileView(requireContext()).apply {
                tileTitle = game.title
                accentColor = getAccentColorForGame(game)
                setOnClickListener {
                    selectGame(game)
                }
                setOnLongClickListener {
                    openGameOptions(game)
                    true
                }
            }

            gameTiles[game.filename] = tileView

            val params = GridLayout.LayoutParams().apply {
                width = dpToPx(110)
                height = dpToPx(110)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                setMargins(6, 6, 6, 6)
            }

            gamesGrid.addView(tileView, params)

            // Animate entrance
            tileView.alpha = 0f
            tileView.scaleX = 0.8f
            tileView.scaleY = 0.8f
            tileView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200 + (index * 30).toLong())
                .start()
        }
    }

    private fun getAccentColorForGame(game: Game): Int {
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#2E7D32"),
            android.graphics.Color.parseColor("#FF6B35"),
            android.graphics.Color.parseColor("#E63946"),
            android.graphics.Color.parseColor("#06A77D"),
            android.graphics.Color.parseColor("#7B68EE"),
            android.graphics.Color.parseColor("#00BCD4")
        )
        return colors[game.filename.hashCode().toInt() % colors.size]
    }

    private fun selectGame(game: Game) {
        selectedGame = game

        // Update visual states
        gameTiles.forEach { (_, tile) ->
            tile.setSelectedState(false, animate = true)
        }
        gameTiles[game.filename]?.setSelectedState(true, animate = true)
    }

    private fun openGameOptions(game: Game) {
        // TODO: Open context menu or options dialog for game
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    companion object {
        fun newInstance() = ThreeDSTopScreenFragment()
    }
}
