// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Achievement entities
 */
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE gameId = :gameId")
    fun getAchievementsByGame(gameId: Long): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE gameId = :gameId AND isAwarded = 1")
    fun getEarnedAchievementsByGame(gameId: Long): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isAwarded = 1 ORDER BY awardedDate DESC LIMIT :limit")
    fun getRecentAchievements(limit: Int = 10): Flow<List<Achievement>>

    @Query("SELECT COUNT(*) FROM achievements WHERE gameId = :gameId")
    fun getTotalAchievementCount(gameId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievements WHERE gameId = :gameId AND isAwarded = 1")
    fun getEarnedAchievementCount(gameId: Long): Flow<Int>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    fun getAchievementById(achievementId: Int): Flow<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Delete
    suspend fun deleteAchievement(achievement: Achievement)

    @Query("DELETE FROM achievements WHERE gameId = :gameId")
    suspend fun deleteAchievementsByGame(gameId: Long)

    @Query("UPDATE achievements SET isAwarded = 1, awardedDate = :awardedDate WHERE id = :achievementId")
    suspend fun awardAchievement(achievementId: Int, awardedDate: Long = System.currentTimeMillis())
}
