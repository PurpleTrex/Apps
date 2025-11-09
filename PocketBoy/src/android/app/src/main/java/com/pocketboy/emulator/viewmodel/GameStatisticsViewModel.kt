// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketboy.emulator.data.GameStatistics
import com.pocketboy.emulator.data.GameStatisticsRepository
import com.pocketboy.emulator.data.UserProfileRepository
import com.pocketboy.emulator.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing game statistics
 */
class GameStatisticsViewModel(
    private val gameStatisticsRepository: GameStatisticsRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _gameStatistics = MutableStateFlow<GameStatistics?>(null)
    val gameStatistics: StateFlow<GameStatistics?> = _gameStatistics.asStateFlow()

    private val _mostRecentGames = MutableStateFlow<List<GameStatistics>>(emptyList())
    val mostRecentGames: StateFlow<List<GameStatistics>> = _mostRecentGames.asStateFlow()

    private val _mostPlayedGames = MutableStateFlow<List<GameStatistics>>(emptyList())
    val mostPlayedGames: StateFlow<List<GameStatistics>> = _mostPlayedGames.asStateFlow()

    private val _completedGames = MutableStateFlow<List<GameStatistics>>(emptyList())
    val completedGames: StateFlow<List<GameStatistics>> = _completedGames.asStateFlow()

    private val _totalPlayTime = MutableStateFlow(0L)
    val totalPlayTime: StateFlow<Long> = _totalPlayTime.asStateFlow()

    private val _gamesPlayed = MutableStateFlow(0)
    val gamesPlayed: StateFlow<Int> = _gamesPlayed.asStateFlow()

    init {
        loadAllStatistics()
    }

    fun loadStatisticsForGame(titleId: Long) {
        viewModelScope.launch {
            try {
                gameStatisticsRepository.getStatisticsForGame(titleId).collect { stats ->
                    _gameStatistics.value = stats
                }
            } catch (e: Exception) {
                Log.error("[GameStatisticsViewModel] Error loading game statistics: ${e.message}")
            }
        }
    }

    private fun loadAllStatistics() {
        viewModelScope.launch {
            try {
                // Load recent games
                gameStatisticsRepository.getMostRecentlyPlayedGames(10).collect { games ->
                    _mostRecentGames.value = games
                }

                // Load most played games
                gameStatisticsRepository.getMostPlayedGames(10).collect { games ->
                    _mostPlayedGames.value = games
                }

                // Load completed games
                gameStatisticsRepository.getCompletedGames().collect { games ->
                    _completedGames.value = games
                }

                // Load total playtime
                gameStatisticsRepository.getTotalPlayTime().collect { total ->
                    _totalPlayTime.value = total ?: 0L
                }

                // Load games played count
                gameStatisticsRepository.getTotalGamesPlayed().collect { count ->
                    _gamesPlayed.value = count
                }
            } catch (e: Exception) {
                Log.error("[GameStatisticsViewModel] Error loading statistics: ${e.message}")
            }
        }
    }

    fun recordGameSession(titleId: Long, playDuration: Long, filename: String) {
        viewModelScope.launch {
            try {
                // Record the session
                gameStatisticsRepository.recordGameSession(titleId, playDuration)

                // Update user profile statistics
                val total = _totalPlayTime.value + playDuration
                val gamesCount = _gamesPlayed.value

                userProfileRepository.updateProfileStatistics(
                    totalPlayTime = total,
                    gamesPlayed = gamesCount,
                    totalAchievements = 0  // Will be updated when achievements sync
                )

                // Reload all statistics
                loadAllStatistics()

                Log.info("[GameStatisticsViewModel] Recorded session for game $titleId: ${playDuration}ms")
            } catch (e: Exception) {
                Log.error("[GameStatisticsViewModel] Error recording game session: ${e.message}")
            }
        }
    }

    fun markGameAsCompleted(titleId: Long) {
        viewModelScope.launch {
            try {
                gameStatisticsRepository.markGameAsCompleted(titleId)
                loadStatisticsForGame(titleId)
                Log.info("[GameStatisticsViewModel] Marked game $titleId as completed")
            } catch (e: Exception) {
                Log.error("[GameStatisticsViewModel] Error marking game as completed: ${e.message}")
            }
        }
    }

    companion object {
        const val TAG = "GameStatisticsViewModel"
    }
}
