// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Achievement entity for storing user achievements from RetroAchievements
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: Int,  // RetroAchievements achievement ID

    val gameId: Long,  // RetroAchievements game ID
    val title: String,
    val description: String,
    val points: Int = 0,
    val badgeUrl: String = "",  // URL to achievement badge/icon
    val isAwarded: Boolean = false,
    val awardedDate: Long = 0L,  // Timestamp when awarded

    // Additional metadata
    val rarity: Float = 0f,  // Percentage of players who have earned this (0-100)
    val difficulty: String = "Unknown"  // Easy, Medium, Hard, etc.
) {
    companion object {
        fun empty() = Achievement(
            id = 0,
            gameId = 0L,
            title = "",
            description = ""
        )
    }
}
