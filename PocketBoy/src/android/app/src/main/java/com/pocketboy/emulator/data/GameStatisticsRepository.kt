// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing game statistics data
 * Abstracts the data layer from the rest of the application
 */
class GameStatisticsRepository(private val gameStatisticsDao: GameStatisticsDao) {

    fun getStatisticsForGame(titleId: Long): Flow<GameStatistics?> {
        return gameStatisticsDao.getStatisticsForGame(titleId)
    }

    fun getMostRecentlyPlayedGames(limit: Int = 10): Flow<List<GameStatistics>> {
        return gameStatisticsDao.getMostRecentlyPlayedGames(limit)
    }

    fun getMostPlayedGames(limit: Int = 10): Flow<List<GameStatistics>> {
        return gameStatisticsDao.getMostPlayedGames(limit)
    }

    fun getCompletedGames(): Flow<List<GameStatistics>> {
        return gameStatisticsDao.getCompletedGames()
    }

    fun getTotalPlayTime(): Flow<Long?> {
        return gameStatisticsDao.getTotalPlayTime()
    }

    fun getTotalGamesPlayed(): Flow<Int> {
        return gameStatisticsDao.getTotalGamesPlayed()
    }

    fun getAveragePlayTimePerGame(): Flow<Long?> {
        return gameStatisticsDao.getAveragePlayTimePerGame()
    }

    suspend fun insertStatistics(statistics: GameStatistics) {
        gameStatisticsDao.insertStatistics(statistics)
    }

    suspend fun updateStatistics(statistics: GameStatistics) {
        gameStatisticsDao.updateStatistics(statistics)
    }

    suspend fun deleteStatistics(statistics: GameStatistics) {
        gameStatisticsDao.deleteStatistics(statistics)
    }

    suspend fun deleteStatisticsForGame(titleId: Long) {
        gameStatisticsDao.deleteStatisticsForGame(titleId)
    }

    suspend fun recordGameSession(titleId: Long, playDuration: Long) {
        gameStatisticsDao.recordGameSession(titleId, playDuration)
    }

    suspend fun updateAchievementProgress(titleId: Long, earned: Int, total: Int) {
        gameStatisticsDao.updateAchievementProgress(titleId, earned, total)
    }

    suspend fun markGameAsCompleted(titleId: Long) {
        gameStatisticsDao.markGameAsCompleted(titleId)
    }

    // Helper function to get or create statistics for a game
    suspend fun getOrCreateStatistics(titleId: Long, filename: String): GameStatistics {
        val existing = gameStatisticsDao.getStatisticsForGame(titleId)
        return try {
            existing.value ?: run {
                val newStats = GameStatistics.empty(titleId, filename)
                insertStatistics(newStats)
                newStats
            }
        } catch (e: Exception) {
            GameStatistics.empty(titleId, filename)
        }
    }
}
