// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketboy.emulator.data.UserProfile
import com.pocketboy.emulator.data.UserProfileRepository
import com.pocketboy.emulator.data.GameStatisticsRepository
import com.pocketboy.emulator.network.RetroAchievementsClient
import com.pocketboy.emulator.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user profile
 */
class UserProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val gameStatisticsRepository: GameStatisticsRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                userProfileRepository.ensureProfileExists()
                userProfileRepository.getUserProfile().collect { profile ->
                    _userProfile.value = profile
                }
            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error loading profile: ${e.message}")
                _errorMessage.value = "Failed to load profile"
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateUsername(newUsername)
                Log.info("[UserProfileViewModel] Username updated to: $newUsername")
            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error updating username: ${e.message}")
                _errorMessage.value = "Failed to update username"
            }
        }
    }

    fun updateProfileImage(imageUrl: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateProfileImage(imageUrl)
                Log.info("[UserProfileViewModel] Profile image updated")
            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error updating profile image: ${e.message}")
            }
        }
    }

    fun linkRetroAchievements(username: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _syncProgress.value = 0

                // Validate username by fetching profile
                val raService = RetroAchievementsClient.getInstance()
                _syncProgress.value = 30

                val raProfile = raService.getUserProfile(username)
                _syncProgress.value = 60

                // Link account
                userProfileRepository.linkRetroAchievements(username)
                _syncProgress.value = 80

                // Update RA stats
                userProfileRepository.updateRetroAchievementsStats(
                    points = raProfile.points,
                    rank = raProfile.rank
                )

                _syncProgress.value = 100
                Log.info("[UserProfileViewModel] RetroAchievements account linked: $username")

            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error linking RetroAchievements: ${e.message}")
                _errorMessage.value = "Failed to link RetroAchievements account: ${e.message}"
            } finally {
                _isLoading.value = false
                _syncProgress.value = 0
            }
        }
    }

    fun unlinkRetroAchievements() {
        viewModelScope.launch {
            try {
                userProfileRepository.unlinkRetroAchievements()
                Log.info("[UserProfileViewModel] RetroAchievements account unlinked")
            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error unlinking RetroAchievements: ${e.message}")
                _errorMessage.value = "Failed to unlink RetroAchievements account"
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userProfileRepository.setDarkMode(enabled)
            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error setting dark mode: ${e.message}")
            }
        }
    }

    fun setAchievementNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userProfileRepository.setAchievementNotifications(enabled)
            } catch (e: Exception) {
                Log.error("[UserProfileViewModel] Error setting achievement notifications: ${e.message}")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    companion object {
        const val TAG = "UserProfileViewModel"
    }
}
