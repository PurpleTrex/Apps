// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pocketboy.emulator.data.AchievementsRepository
import com.pocketboy.emulator.data.GameStatisticsRepository
import com.pocketboy.emulator.data.PocketBoyDatabase
import com.pocketboy.emulator.data.UserProfileRepository

/**
 * Factory for creating ViewModels with proper dependency injection
 */
class PocketBoyViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val database = PocketBoyDatabase.getInstance(context)
    private val achievementsRepository = AchievementsRepository(database.achievementDao())
    private val gameStatisticsRepository = GameStatisticsRepository(database.gameStatisticsDao())
    private val userProfileRepository = UserProfileRepository(database.userProfileDao())

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AchievementsViewModel::class.java -> {
                AchievementsViewModel(
                    achievementsRepository,
                    gameStatisticsRepository,
                    userProfileRepository
                ) as T
            }
            GameStatisticsViewModel::class.java -> {
                GameStatisticsViewModel(
                    gameStatisticsRepository,
                    userProfileRepository
                ) as T
            }
            UserProfileViewModel::class.java -> {
                UserProfileViewModel(
                    userProfileRepository,
                    gameStatisticsRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
