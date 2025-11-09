// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UserProfile entity
 */
@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("""
        UPDATE user_profile
        SET totalPlayTime = :totalPlayTime,
            gamesPlayed = :gamesPlayed,
            totalAchievements = :totalAchievements
        WHERE id = 1
    """)
    suspend fun updateProfileStatistics(
        totalPlayTime: Long,
        gamesPlayed: Int,
        totalAchievements: Int
    )

    @Query("UPDATE user_profile SET retroAchievementsUsername = :username, isRetroAchievementsLinked = 1, raLastSyncDate = :syncDate WHERE id = 1")
    suspend fun linkRetroAchievements(username: String, syncDate: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET isRetroAchievementsLinked = 0, retroAchievementsUsername = '' WHERE id = 1")
    suspend fun unlinkRetroAchievements()

    @Query("UPDATE user_profile SET raPoints = :points, raRank = :rank, raLastSyncDate = :syncDate WHERE id = 1")
    suspend fun updateRetroAchievementsStats(points: Int, rank: String, syncDate: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET username = :username WHERE id = 1")
    suspend fun updateUsername(username: String)

    @Query("UPDATE user_profile SET profileImageUrl = :imageUrl WHERE id = 1")
    suspend fun updateProfileImage(imageUrl: String)

    @Query("UPDATE user_profile SET darkModeEnabled = :enabled WHERE id = 1")
    suspend fun setDarkMode(enabled: Boolean)

    @Query("UPDATE user_profile SET showAchievementNotifications = :enabled WHERE id = 1")
    suspend fun setAchievementNotifications(enabled: Boolean)
}
