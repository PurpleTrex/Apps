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
 * Data Access Object for GameStatistics entities
 */
@Dao
interface GameStatisticsDao {
    @Query("SELECT * FROM game_statistics WHERE titleId = :titleId")
    fun getStatisticsForGame(titleId: Long): Flow<GameStatistics?>

    @Query("SELECT * FROM game_statistics ORDER BY lastPlayedDate DESC LIMIT :limit")
    fun getMostRecentlyPlayedGames(limit: Int = 10): Flow<List<GameStatistics>>

    @Query("SELECT * FROM game_statistics WHERE totalPlayTime > 0 ORDER BY totalPlayTime DESC LIMIT :limit")
    fun getMostPlayedGames(limit: Int = 10): Flow<List<GameStatistics>>

    @Query("SELECT * FROM game_statistics WHERE isCompleted = 1 ORDER BY lastPlayedDate DESC")
    fun getCompletedGames(): Flow<List<GameStatistics>>

    @Query("SELECT SUM(totalPlayTime) FROM game_statistics")
    fun getTotalPlayTime(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM game_statistics WHERE totalPlayTime > 0")
    fun getTotalGamesPlayed(): Flow<Int>

    @Query("SELECT AVG(totalPlayTime) FROM game_statistics WHERE totalPlayTime > 0")
    fun getAveragePlayTimePerGame(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: GameStatistics)

    @Update
    suspend fun updateStatistics(statistics: GameStatistics)

    @Delete
    suspend fun deleteStatistics(statistics: GameStatistics)

    @Query("DELETE FROM game_statistics WHERE titleId = :titleId")
    suspend fun deleteStatisticsForGame(titleId: Long)

    @Query("""
        UPDATE game_statistics
        SET totalPlayTime = totalPlayTime + :playDuration,
            totalSessions = totalSessions + 1,
            lastPlayedDate = :currentTime,
            averageSessionLength = totalPlayTime / CAST(totalSessions AS REAL)
        WHERE titleId = :titleId
    """)
    suspend fun recordGameSession(titleId: Long, playDuration: Long, currentTime: Long = System.currentTimeMillis())

    @Query("UPDATE game_statistics SET achievementsEarned = :earned, totalAchievements = :total WHERE titleId = :titleId")
    suspend fun updateAchievementProgress(titleId: Long, earned: Int, total: Int)

    @Query("UPDATE game_statistics SET isCompleted = 1, completionPercentage = 100.0 WHERE titleId = :titleId")
    suspend fun markGameAsCompleted(titleId: Long)
}
