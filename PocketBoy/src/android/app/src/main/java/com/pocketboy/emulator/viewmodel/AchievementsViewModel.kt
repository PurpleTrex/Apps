// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketboy.emulator.data.Achievement
import com.pocketboy.emulator.data.AchievementsRepository
import com.pocketboy.emulator.data.GameStatisticsRepository
import com.pocketboy.emulator.data.UserProfileRepository
import com.pocketboy.emulator.network.RetroAchievementsClient
import com.pocketboy.emulator.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing achievements
 */
class AchievementsViewModel(
    private val achievementsRepository: AchievementsRepository,
    private val gameStatisticsRepository: GameStatisticsRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _currentGameAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val currentGameAchievements: StateFlow<List<Achievement>> = _currentGameAchievements.asStateFlow()

    private val _recentAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val recentAchievements: StateFlow<List<Achievement>> = _recentAchievements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress.asStateFlow()

    init {
        loadRecentAchievements()
    }

    fun loadGameAchievements(gameId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                achievementsRepository.getAchievementsByGame(gameId).collect { achievements ->
                    _currentGameAchievements.value = achievements
                }
            } catch (e: Exception) {
                Log.error("[AchievementsViewModel] Error loading game achievements: ${e.message}")
                _errorMessage.value = "Failed to load achievements"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadRecentAchievements() {
        viewModelScope.launch {
            try {
                achievementsRepository.getRecentAchievements(10).collect { achievements ->
                    _recentAchievements.value = achievements
                }
            } catch (e: Exception) {
                Log.error("[AchievementsViewModel] Error loading recent achievements: ${e.message}")
            }
        }
    }

    fun syncWithRetroAchievements(gameId: Long, username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _syncProgress.value = 0

                val raService = RetroAchievementsClient.getInstance()
                _syncProgress.value = 25

                val response = raService.getGameAchievements(gameId, username)
                _syncProgress.value = 50

                // Convert API response to Achievement entities
                val achievements = mutableListOf<Achievement>()
                response.achievements.forEach { (_, data) ->
                    val achievement = Achievement(
                        id = data.id,
                        gameId = gameId,
                        title = data.title,
                        description = data.description,
                        points = data.points,
                        badgeUrl = "${data.badgeUrl}",
                        isAwarded = data.dateEarned != null,
                        awardedDate = data.dateEarned?.let { parseDate(it) } ?: 0L,
                        rarity = data.rarity,
                        difficulty = "Unknown"
                    )
                    achievements.add(achievement)
                }

                _syncProgress.value = 75

                // Save achievements to database
                achievementsRepository.insertAchievements(achievements)

                // Update game statistics with achievement progress
                if (response.userCompletion != null) {
                    gameStatisticsRepository.updateAchievementProgress(
                        gameId,
                        response.userCompletion.numAchieved,
                        response.userCompletion.totalAchievements
                    )
                }

                _syncProgress.value = 100
                _currentGameAchievements.value = achievements
                Log.info("[AchievementsViewModel] Synced ${achievements.size} achievements for game $gameId")

            } catch (e: Exception) {
                Log.error("[AchievementsViewModel] Error syncing achievements: ${e.message}")
                _errorMessage.value = "Failed to sync achievements: ${e.message}"
            } finally {
                _isLoading.value = false
                _syncProgress.value = 0
            }
        }
    }

    fun awardAchievement(achievementId: Int) {
        viewModelScope.launch {
            try {
                achievementsRepository.awardAchievement(achievementId)
                Log.info("[AchievementsViewModel] Achievement $achievementId awarded")
            } catch (e: Exception) {
                Log.error("[AchievementsViewModel] Error awarding achievement: ${e.message}")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun parseDate(dateString: String): Long {
        return try {
            // RetroAchievements date format: "2024-01-15 10:30:45"
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            formatter.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    companion object {
        const val TAG = "AchievementsViewModel"
    }
}
