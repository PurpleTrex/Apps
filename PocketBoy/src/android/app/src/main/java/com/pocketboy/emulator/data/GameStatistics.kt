// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * GameStatistics entity for tracking playtime and game progress
 */
@Entity(tableName = "game_statistics")
data class GameStatistics(
    @PrimaryKey
    val titleId: Long,  // Game title ID (primary key from Game model)

    val filename: String = "",
    val totalPlayTime: Long = 0L,  // Total playtime in milliseconds
    val totalSessions: Int = 0,  // Number of times game has been launched
    val lastPlayedDate: Long = 0L,  // Last play timestamp
    val firstPlayedDate: Long = 0L,  // First play timestamp
    val averageSessionLength: Long = 0L,  // Average playtime per session in ms

    // Achievement tracking
    val achievementsEarned: Int = 0,  // Number of achievements earned
    val totalAchievements: Int = 0,  // Total available achievements
    val achievementPercentage: Float = 0f,  // Percentage of achievements (0-100)

    // Game progress
    val isCompleted: Boolean = false,
    val completionPercentage: Float = 0f,  // Overall completion percentage (0-100)

    // Gameplay stats
    val difficulty: String = "Normal"  // Selected difficulty level
) {
    companion object {
        fun empty(titleId: Long, filename: String = "") = GameStatistics(
            titleId = titleId,
            filename = filename
        )
    }
}
