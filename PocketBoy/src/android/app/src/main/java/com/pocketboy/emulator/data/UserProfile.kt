// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * UserProfile entity for storing user information and statistics
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,  // Single row for user profile (always 1)

    val username: String = "Player",
    val retroAchievementsUsername: String = "",  // RetroAchievements username
    val isRetroAchievementsLinked: Boolean = false,
    val profileImageUrl: String = "",
    val createdDate: Long = System.currentTimeMillis(),

    // Statistics aggregates
    val totalPlayTime: Long = 0L,  // Total playtime across all games in ms
    val gamesPlayed: Int = 0,  // Number of unique games played
    val totalAchievements: Int = 0,  // Total achievements earned across all games
    val totalGamesCompleted: Int = 0,  // Number of games marked as completed

    // RetroAchievements stats
    val raPoints: Int = 0,  // RetroAchievements points earned
    val raRank: String = "",  // Rank badge ("Bronze", "Silver", "Gold", etc.)
    val raLastSyncDate: Long = 0L,  // Last sync with RetroAchievements

    // Preferences
    val preferredLanguage: String = "English",
    val darkModeEnabled: Boolean = false,
    val showAchievementNotifications: Boolean = true
) {
    companion object {
        fun default() = UserProfile(
            id = 1,
            username = "Player",
            createdDate = System.currentTimeMillis()
        )
    }
}
